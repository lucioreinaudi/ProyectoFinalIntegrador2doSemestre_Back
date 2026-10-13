package com.example.demo.controlador;

import com.example.demo.dto.NotificacionCrearRequest;
import com.example.demo.dto.NotificacionResponse;
import com.example.demo.modelo.Notificacion;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.NotificacionRepository;
import com.example.demo.repositorio.ReclamoRepository;
import com.example.demo.repositorio.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReclamoRepository reclamoRepository;

    public NotificacionController(NotificacionRepository notificacionRepository, UsuarioRepository usuarioRepository,
                                   ReclamoRepository reclamoRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.reclamoRepository = reclamoRepository;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<NotificacionResponse> porUsuario(@PathVariable Long usuarioId) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId)
                .stream().map(NotificacionResponse::de).toList();
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public List<NotificacionResponse> noLeidas(@PathVariable Long usuarioId) {
        return notificacionRepository.findByUsuarioIdAndLeidaFalse(usuarioId)
                .stream().map(NotificacionResponse::de).toList();
    }

    @GetMapping("/usuario/{usuarioId}/no-leidas/contador")
    public Map<String, Long> contadorNoLeidas(@PathVariable Long usuarioId) {
        return Map.of("cantidad", notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId));
    }

    @PostMapping
    public ResponseEntity<NotificacionResponse> crear(@Valid @RequestBody NotificacionCrearRequest request) {
        Usuario usuario = usuarioRepository.findById(request.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el usuario indicado"));
        Reclamo reclamo = reclamoRepository.findById(request.reclamoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reclamo indicado"));

        Notificacion notificacion = new Notificacion(usuario, reclamo, request.mensaje());
        Notificacion guardada = notificacionRepository.save(notificacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(NotificacionResponse.de(guardada));
    }

    @PatchMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable Long id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa notificacion"));
        notificacion.setLeida(true);
        return NotificacionResponse.de(notificacionRepository.save(notificacion));
    }
}
