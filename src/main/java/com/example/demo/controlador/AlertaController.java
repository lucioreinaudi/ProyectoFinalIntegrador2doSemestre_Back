package com.example.demo.controlador;

import com.example.demo.dto.AlertaActualizarEstadoRequest;
import com.example.demo.dto.AlertaCrearRequest;
import com.example.demo.dto.AlertaResponse;
import com.example.demo.modelo.Alerta;
import com.example.demo.modelo.Usuario;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.AlertaRepository;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.repositorio.ZonaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    // Mismos valores que el CHECK de la tabla Alerta en la BD.
    private static final Set<String> ESTADOS_VALIDOS = Set.of("activa", "finalizada");

    // Mismos valores que el CHECK de tipo en la tabla Alerta.
    private static final Set<String> TIPOS_VALIDOS = Set.of("tormenta", "viento", "electricidad", "arbol", "otro");

    private final AlertaRepository alertaRepository;
    private final ZonaRepository zonaRepository;
    private final UsuarioRepository usuarioRepository;

    public AlertaController(AlertaRepository alertaRepository, ZonaRepository zonaRepository, UsuarioRepository usuarioRepository) {
        this.alertaRepository = alertaRepository;
        this.zonaRepository = zonaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<AlertaResponse> listar() {
        return alertaRepository.findAll().stream().map(AlertaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public AlertaResponse obtener(@PathVariable Long id) {
        return AlertaResponse.de(buscarOFallar(id));
    }

    @GetMapping("/estado/{estado}")
    public List<AlertaResponse> porEstado(@PathVariable String estado) {
        return alertaRepository.findByEstado(estado).stream().map(AlertaResponse::de).toList();
    }

    @GetMapping("/zona/{zonaId}/estado/{estado}")
    public List<AlertaResponse> porZonaYEstado(@PathVariable Long zonaId, @PathVariable String estado) {
        return alertaRepository.findByZonaIdAndEstado(zonaId, estado).stream().map(AlertaResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<AlertaResponse> crear(@Valid @RequestBody AlertaCrearRequest request, Authentication authentication) {
        // Quien emite la alerta es quien esta logueado, no un id que mande el cliente.
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        Zona zona = zonaRepository.findById(request.zonaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));

        String tipo = (request.tipo() == null || request.tipo().isBlank()) ? "otro" : request.tipo();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo invalido. Valores permitidos: " + TIPOS_VALIDOS);
        }

        Alerta alerta = new Alerta();
        alerta.setTipo(tipo);
        alerta.setImagenUrl(request.imagenUrl());
        alerta.setTitulo(request.titulo());
        alerta.setDescripcion(request.descripcion());
        alerta.setNivelGravedad(request.nivelGravedad());
        alerta.setRecomendaciones(request.recomendaciones());
        alerta.setZona(zona);
        alerta.setUsuario(usuario);

        Alerta guardada = alertaRepository.save(alerta);
        return ResponseEntity.status(HttpStatus.CREATED).body(AlertaResponse.de(guardada));
    }

    @PatchMapping("/{id}/estado")
    public AlertaResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody AlertaActualizarEstadoRequest request) {
        if (!ESTADOS_VALIDOS.contains(request.estado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado invalido. Valores permitidos: " + ESTADOS_VALIDOS);
        }
        Alerta alerta = buscarOFallar(id);
        alerta.setEstado(request.estado());
        return AlertaResponse.de(alertaRepository.save(alerta));
    }

    private Alerta buscarOFallar(Long id) {
        return alertaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa alerta"));
    }
}
