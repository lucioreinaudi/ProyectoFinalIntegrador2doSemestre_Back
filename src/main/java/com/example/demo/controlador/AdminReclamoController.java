package com.example.demo.controlador;

import com.example.demo.dto.PaginaResponse;
import com.example.demo.dto.ReclamoDetalleResponse;
import com.example.demo.dto.ReclamoReclasificarRequest;
import com.example.demo.dto.ReclamoResponse;
import com.example.demo.dto.ReclamoResumenResponse;
import com.example.demo.modelo.Categoria;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.CategoriaRepository;
import com.example.demo.repositorio.ReclamoRepository;
import com.example.demo.repositorio.ZonaRepository;
import com.example.demo.service.ReclamoConsultaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

// Endpoints del panel de administracion sobre reclamos. Todo /api/admin/** es
// solo para el municipio (regla en SecurityConfig).
@RestController
@RequestMapping("/api/admin/reclamos")
public class AdminReclamoController {

    private final ReclamoRepository reclamoRepository;
    private final ZonaRepository zonaRepository;
    private final CategoriaRepository categoriaRepository;
    private final ReclamoConsultaService reclamoConsultaService;

    public AdminReclamoController(ReclamoRepository reclamoRepository, ZonaRepository zonaRepository,
                                  CategoriaRepository categoriaRepository, ReclamoConsultaService reclamoConsultaService) {
        this.reclamoRepository = reclamoRepository;
        this.zonaRepository = zonaRepository;
        this.categoriaRepository = categoriaRepository;
        this.reclamoConsultaService = reclamoConsultaService;
    }

    // Listado de la pestana Reclamos: estado actual, publicado, filtros opcionales
    // y paginacion (mas recientes primero). Fechas en formato AAAA-MM-DD.
    @GetMapping
    public PaginaResponse<ReclamoResumenResponse> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Long zonaId,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Boolean publicado,
            @RequestParam(required = false) Boolean soloOtro,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanio) {
        ReclamoConsultaService.Filtros filtros = new ReclamoConsultaService.Filtros(
                estado, zonaId, categoriaId, desde, hasta, publicado, soloOtro);
        return reclamoConsultaService.listarParaPanel(filtros, pagina, tamanio);
    }

    // Detalle: historial completo, datos de contacto del vecino y alertas vinculadas.
    @GetMapping("/{id}")
    public ReclamoDetalleResponse detalle(@PathVariable Long id) {
        return reclamoConsultaService.detalle(id);
    }

    // Reclasificar zona y/o categoria (pensado sobre todo para los reclamos "(Otro)").
    // El texto del vecino (titulo, descripcion, foto, ubicacion) no se toca nunca (Q9).
    // Solo se puede elegir una zona/categoria activa (Q29). No notifica al vecino:
    // las notificaciones son solo por cambio de estado y por publicacion (Q11, Q30).
    @PatchMapping("/{id}")
    @Transactional
    public ReclamoResponse reclasificar(@PathVariable Long id, @RequestBody ReclamoReclasificarRequest request) {
        if (request.zonaId() == null && request.categoriaId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Indica zonaId, categoriaId o los dos");
        }

        Reclamo reclamo = reclamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));

        if (request.zonaId() != null) {
            Zona zona = zonaRepository.findById(request.zonaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));
            if (!zona.isActiva()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La zona indicada esta dada de baja");
            }
            reclamo.setZona(zona);
        }

        if (request.categoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(request.categoriaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la categoria indicada"));
            if (!categoria.isActiva()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La categoria indicada esta dada de baja");
            }
            reclamo.setCategoria(categoria);
        }

        return ReclamoResponse.de(reclamoRepository.save(reclamo));
    }
}
