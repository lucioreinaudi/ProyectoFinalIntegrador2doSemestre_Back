package com.example.demo.controlador;

import com.example.demo.dto.AlertaActualizarEstadoRequest;
import com.example.demo.dto.AlertaCrearRequest;
import com.example.demo.dto.AlertaResponse;
import com.example.demo.modelo.Alerta;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.AlertaRepository;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.service.AlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Las reglas (niveles, tipos, zona activa, cuando se puede eliminar) estan en
// AlertaService. Quien puede llamar a cada endpoint esta en SecurityConfig:
// - GET /estado/activa y GET /{id}: publicos (las alertas se ven sin login).
// - Crear, editar, cambiar estado y eliminar: solo municipio.
@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaRepository alertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AlertaService alertaService;

    public AlertaController(AlertaRepository alertaRepository, UsuarioRepository usuarioRepository,
                            AlertaService alertaService) {
        this.alertaRepository = alertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.alertaService = alertaService;
    }

    @GetMapping
    public List<AlertaResponse> listar() {
        return alertaService.responder(alertaRepository.findAllByOrderByFechaEmisionDescIdDesc());
    }

    @GetMapping("/{id}")
    public AlertaResponse obtener(@PathVariable Long id) {
        return alertaService.responder(alertaService.buscarOFallar(id));
    }

    @GetMapping("/estado/{estado}")
    public List<AlertaResponse> porEstado(@PathVariable String estado) {
        return alertaService.responder(alertaRepository.findByEstadoOrderByFechaEmisionDescIdDesc(estado));
    }

    @GetMapping("/zona/{zonaId}/estado/{estado}")
    public List<AlertaResponse> porZonaYEstado(@PathVariable Long zonaId, @PathVariable String estado) {
        return alertaService.responder(alertaRepository.findByZonaIdAndEstadoOrderByFechaEmisionDescIdDesc(zonaId, estado));
    }

    // Alerta creada 100% por el municipio, sin reclamo de por medio (seccion 6.3).
    @PostMapping
    public ResponseEntity<AlertaResponse> crear(@Valid @RequestBody AlertaCrearRequest request, Authentication authentication) {
        // Quien emite la alerta es quien esta logueado, no un id que mande el cliente.
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        Alerta guardada = alertaService.crear(request, usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(alertaService.responder(guardada));
    }

    @PutMapping("/{id}")
    public AlertaResponse editar(@PathVariable Long id, @Valid @RequestBody AlertaCrearRequest request) {
        return alertaService.responder(alertaService.editar(id, request));
    }

    // Finalizar / reactivar.
    @PatchMapping("/{id}/estado")
    public AlertaResponse actualizarEstado(@PathVariable Long id, @Valid @RequestBody AlertaActualizarEstadoRequest request) {
        return alertaService.responder(alertaService.cambiarEstado(id, request.estado()));
    }

    // 409 si tiene reclamos vinculados (ver AlertaService.eliminar).
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        alertaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
