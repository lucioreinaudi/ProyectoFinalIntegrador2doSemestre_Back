package com.example.demo.service;

import com.example.demo.dto.AlertaCrearRequest;
import com.example.demo.dto.AlertaResponse;
import com.example.demo.modelo.Alerta;
import com.example.demo.modelo.Usuario;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.AlertaReclamoRepository;
import com.example.demo.repositorio.AlertaRepository;
import com.example.demo.repositorio.ZonaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Reglas de las alertas (especificacion, seccion 6). Esta en un service porque
// las usan dos lugares: el ABM de alertas (AlertaController) y "publicar
// reclamo" (PublicacionService), que crea una alerta con las mismas validaciones.
@Service
public class AlertaService {

    // Mismos valores que los CHECK de la tabla Alerta en la BD.
    public static final Set<String> NIVELES = Set.of("alto", "medio", "bajo");
    // Los tipos ya no son una lista fija: viven en la tabla TipoAlerta (SCRUM-94).
    public static final Set<String> ESTADOS = Set.of("activa", "finalizada");

    private final AlertaRepository alertaRepository;
    private final AlertaReclamoRepository alertaReclamoRepository;
    private final ZonaRepository zonaRepository;
    private final StorageService storageService;
    private final TipoAlertaService tipoAlertaService;

    public AlertaService(AlertaRepository alertaRepository, AlertaReclamoRepository alertaReclamoRepository,
                         ZonaRepository zonaRepository, StorageService storageService,
                         TipoAlertaService tipoAlertaService) {
        this.alertaRepository = alertaRepository;
        this.alertaReclamoRepository = alertaReclamoRepository;
        this.zonaRepository = zonaRepository;
        this.storageService = storageService;
        this.tipoAlertaService = tipoAlertaService;
    }

    @Transactional
    public Alerta crear(AlertaCrearRequest datos, Usuario autor) {
        Alerta alerta = new Alerta();
        aplicarDatos(alerta, datos);
        alerta.setUsuario(autor);
        return alertaRepository.save(alerta);
    }

    // Editar se permite en cualquier momento, activa o finalizada (Q19).
    // Reemplaza todos los datos (PUT); el estado se cambia aparte (PATCH .../estado).
    @Transactional
    public Alerta editar(Long id, AlertaCrearRequest datos) {
        Alerta alerta = buscarOFallar(id);
        aplicarDatos(alerta, datos);
        return alertaRepository.save(alerta);
    }

    @Transactional
    public Alerta cambiarEstado(Long id, String estado) {
        if (!ESTADOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido. Valores permitidos: " + ESTADOS);
        }
        Alerta alerta = buscarOFallar(id);
        alerta.setEstado(estado);
        return alertaRepository.save(alerta);
    }

    // Eliminar es solo para alertas creadas por error: si tiene reclamos
    // vinculados no se borra (409); en ese caso hay que finalizarla (Q19).
    @Transactional
    public void eliminar(Long id) {
        Alerta alerta = buscarOFallar(id);
        if (alertaReclamoRepository.existsByAlertaId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La alerta tiene reclamos vinculados: no se puede eliminar, hay que finalizarla");
        }
        alertaRepository.delete(alerta);
    }

    public Alerta buscarOFallar(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa alerta"));
    }

    // --- Armado de respuestas con la cantidad de reclamos vinculados ---

    @Transactional(readOnly = true)
    public AlertaResponse responder(Alerta alerta) {
        return AlertaResponse.de(alerta, alertaReclamoRepository.countByAlertaId(alerta.getId()));
    }

    // Para listas: una sola consulta de conteo para todas las alertas.
    @Transactional(readOnly = true)
    public List<AlertaResponse> responder(List<Alerta> alertas) {
        if (alertas.isEmpty()) {
            return List.of();
        }
        Map<Long, Long> cantidades = new HashMap<>();
        for (Object[] fila : alertaReclamoRepository.contarPorAlerta(alertas.stream().map(Alerta::getId).toList())) {
            cantidades.put((Long) fila[0], (Long) fila[1]);
        }
        return alertas.stream().map(a -> AlertaResponse.de(a, cantidades.getOrDefault(a.getId(), 0L))).toList();
    }

    // La imagen tiene que ser de nuestro Storage: una subida desde el panel o una
    // foto de un reclamo. Si al editar se deja la que ya tenia, se acepta igual.
    private String validarImagen(Alerta alerta, String imagenUrl) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return null;
        }
        if (!imagenUrl.equals(alerta.getImagenUrl()) && !storageService.esUrlPropia(imagenUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La imagen tiene que ser una foto del reclamo o una subida desde el panel");
        }
        return imagenUrl;
    }

    // Valida y copia los datos del formulario a la alerta (nueva o existente).
    private void aplicarDatos(Alerta alerta, AlertaCrearRequest datos) {
        if (!NIVELES.contains(datos.nivelGravedad())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nivel de gravedad invalido. Valores permitidos: " + NIVELES);
        }

        String tipo = (datos.tipo() == null || datos.tipo().isBlank()) ? "otro" : datos.tipo();
        // alerta.getTipo() es el que ya tenia (null si es nueva; el default "otro"
        // de la entidad no cuenta porque todavia no se guardo).
        tipoAlertaService.validarParaAlerta(tipo, alerta.getId() != null ? alerta.getTipo() : null);

        Zona zona = zonaRepository.findById(datos.zonaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));
        // Solo zonas activas (Q29). Excepcion: al editar, si la alerta ya estaba en
        // esa zona y despues la dieron de baja, se puede seguir editando sin moverla.
        boolean esLaMismaZona = alerta.getZona() != null && alerta.getZona().getId().equals(zona.getId());
        if (!zona.isActiva() && !esLaMismaZona) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La zona indicada esta dada de baja");
        }

        alerta.setTitulo(datos.titulo().trim());
        alerta.setDescripcion(datos.descripcion().trim());
        alerta.setNivelGravedad(datos.nivelGravedad());
        alerta.setRecomendaciones(datos.recomendaciones());
        alerta.setTipo(tipo);
        alerta.setImagenUrl(validarImagen(alerta, datos.imagenUrl()));
        alerta.setZona(zona);
    }
}
