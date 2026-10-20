package com.example.demo.service;

import com.example.demo.dto.EstadoReclamoResponse;
import com.example.demo.dto.PaginaResponse;
import com.example.demo.dto.ReclamoDetalleResponse;
import com.example.demo.dto.ReclamoResumenResponse;
import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Reclamo;
import com.example.demo.repositorio.AlertaReclamoRepository;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.ReclamoRepository;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Consultas de reclamos con su estado actual y si estan publicados, sin N+1:
// por cada pagina se hacen 3 consultas fijas (reclamos, estados, publicados),
// y no una extra por reclamo como hacia antes Mis Reportes.
@Service
@Transactional(readOnly = true)
public class ReclamoConsultaService {

    // Laboulaye: los filtros "desde/hasta" son dias de aca, no de UTC.
    private static final ZoneId ZONA_HORARIA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final int TAMANIO_MAXIMO = 100;

    private final ReclamoRepository reclamoRepository;
    private final EstadoReclamoRepository estadoReclamoRepository;
    private final AlertaReclamoRepository alertaReclamoRepository;

    public ReclamoConsultaService(ReclamoRepository reclamoRepository, EstadoReclamoRepository estadoReclamoRepository,
                                  AlertaReclamoRepository alertaReclamoRepository) {
        this.reclamoRepository = reclamoRepository;
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.alertaReclamoRepository = alertaReclamoRepository;
    }

    // Filtros del listado del panel. Todos opcionales (null = no filtrar).
    public record Filtros(String estado, Long zonaId, Long categoriaId, LocalDate desde, LocalDate hasta,
                          Boolean publicado, Boolean soloOtro) {
    }

    public PaginaResponse<ReclamoResumenResponse> listarParaPanel(Filtros f, int pagina, int tamanio) {
        if (f.estado() != null && !ReclamoEstadoService.ESTADOS.contains(f.estado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado invalido. Valores permitidos: " + ReclamoEstadoService.ESTADOS);
        }
        if (f.desde() != null && f.hasta() != null && f.desde().isAfter(f.hasta())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'desde' no puede ser posterior a 'hasta'");
        }
        if (pagina < 0 || tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pagina tiene que ser 0 o mas, y tamanio entre 1 y " + TAMANIO_MAXIMO);
        }

        // Orden fijo: mas recientes primero (el id desempata).
        PageRequest pageRequest = PageRequest.of(pagina, tamanio,
                Sort.by(Sort.Direction.DESC, "fechaCreacion").and(Sort.by(Sort.Direction.DESC, "id")));

        Page<Reclamo> page = reclamoRepository.findAll(armarFiltro(f), pageRequest);
        return PaginaResponse.de(page, resumir(page.getContent()));
    }

    public List<ReclamoResumenResponse> listarDelVecino(Long usuarioId) {
        return resumir(reclamoRepository.findByUsuarioIdOrderByFechaCreacionDescIdDesc(usuarioId));
    }

    public ReclamoDetalleResponse detalle(Long reclamoId) {
        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));

        List<EstadoReclamoResponse> historial = estadoReclamoRepository.findByReclamoIdOrderByFechaAscIdAsc(reclamoId)
                .stream().map(EstadoReclamoResponse::de).toList();
        List<ReclamoDetalleResponse.AlertaVinculada> alertas = alertaReclamoRepository
                .findByReclamoIdOrderByFechaVinculacionAsc(reclamoId)
                .stream().map(ReclamoDetalleResponse.AlertaVinculada::de).toList();

        return ReclamoDetalleResponse.de(reclamo, historial, alertas);
    }

    // Agrega estado actual y "publicado" a una lista de reclamos con 2 consultas en total.
    private List<ReclamoResumenResponse> resumir(List<Reclamo> reclamos) {
        if (reclamos.isEmpty()) {
            return List.of();
        }
        List<Long> ids = reclamos.stream().map(Reclamo::getId).toList();

        // Vienen del mas nuevo al mas viejo: el primero de cada reclamo es el actual.
        Map<Long, EstadoReclamo> estadoActual = new HashMap<>();
        for (EstadoReclamo e : estadoReclamoRepository.findByReclamoIdInOrderByFechaDescIdDesc(ids)) {
            estadoActual.putIfAbsent(e.getReclamo().getId(), e);
        }
        Set<Long> publicados = alertaReclamoRepository.findReclamoIdsPublicados(ids);

        return reclamos.stream()
                .map(r -> ReclamoResumenResponse.de(r, estadoActual.get(r.getId()), publicados.contains(r.getId())))
                .toList();
    }

    private Specification<Reclamo> armarFiltro(Filtros f) {
        Specification<Reclamo> spec = Specification.unrestricted();

        if (f.zonaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("zona").get("id"), f.zonaId()));
        }
        if (f.categoriaId() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), f.categoriaId()));
        }
        if (f.desde() != null) {
            OffsetDateTime inicio = f.desde().atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.<OffsetDateTime>get("fechaCreacion"), inicio));
        }
        if (f.hasta() != null) {
            // "hasta" incluye todo ese dia: menor que el inicio del dia siguiente.
            OffsetDateTime fin = f.hasta().plusDays(1).atStartOfDay(ZONA_HORARIA).toOffsetDateTime();
            spec = spec.and((root, query, cb) -> cb.lessThan(root.<OffsetDateTime>get("fechaCreacion"), fin));
        }
        if (Boolean.TRUE.equals(f.soloOtro())) {
            // Por la categoria "Otro", no por el sufijo "(Otro)" del titulo, que
            // depende de como lo escriba el front.
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("categoria").get("nombre")), "otro"));
        }
        if (f.publicado() != null) {
            spec = spec.and((root, query, cb) -> {
                Subquery<Integer> vinculo = query.subquery(Integer.class);
                Root<AlertaReclamo> ar = vinculo.from(AlertaReclamo.class);
                vinculo.select(cb.literal(1)).where(cb.equal(ar.get("reclamo"), root));
                return f.publicado() ? cb.exists(vinculo) : cb.not(cb.exists(vinculo));
            });
        }
        if (f.estado() != null) {
            // Estado ACTUAL = existe un estado con ese valor y no hay ninguno mas nuevo
            // para el mismo reclamo (mismo criterio de "mas nuevo": fecha, y si empatan, id).
            spec = spec.and((root, query, cb) -> {
                Subquery<Integer> conEstado = query.subquery(Integer.class);
                Root<EstadoReclamo> e = conEstado.from(EstadoReclamo.class);

                Subquery<Integer> masNuevo = query.subquery(Integer.class);
                Root<EstadoReclamo> e2 = masNuevo.from(EstadoReclamo.class);
                masNuevo.select(cb.literal(1)).where(
                        cb.equal(e2.get("reclamo"), e.get("reclamo")),
                        cb.or(
                                cb.greaterThan(e2.<OffsetDateTime>get("fecha"), e.<OffsetDateTime>get("fecha")),
                                cb.and(
                                        cb.equal(e2.get("fecha"), e.get("fecha")),
                                        cb.greaterThan(e2.<Long>get("id"), e.<Long>get("id"))
                                )
                        )
                );

                conEstado.select(cb.literal(1)).where(
                        cb.equal(e.get("reclamo"), root),
                        cb.equal(e.get("estado"), f.estado()),
                        cb.not(cb.exists(masNuevo))
                );
                return cb.exists(conEstado);
            });
        }
        return spec;
    }
}
