package com.example.demo.controlador;

import com.example.demo.dto.EstadoReclamoCrearRequest;
import com.example.demo.dto.EstadoReclamoResponse;
import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.ReclamoRepository;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.service.ReclamoEstadoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/estados-reclamo")
public class EstadoReclamoController {

    private final EstadoReclamoRepository estadoReclamoRepository;
    private final ReclamoRepository reclamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ReclamoEstadoService reclamoEstadoService;

    public EstadoReclamoController(EstadoReclamoRepository estadoReclamoRepository, ReclamoRepository reclamoRepository,
                                    UsuarioRepository usuarioRepository, ReclamoEstadoService reclamoEstadoService) {
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.reclamoRepository = reclamoRepository;
        this.usuarioRepository = usuarioRepository;
        this.reclamoEstadoService = reclamoEstadoService;
    }

    // Un vecino solo puede ver el historial de SUS PROPIOS reclamos (lo necesita
    // MisReportes.jsx). El municipio puede ver el de cualquiera. Esto no se puede
    // resolver con una regla de SecurityConfig (necesita saber de quien es el
    // reclamo), por eso el chequeo va aca.
    @GetMapping("/reclamo/{reclamoId}")
    public List<EstadoReclamoResponse> historial(@PathVariable Long reclamoId, Authentication authentication) {
        validarAcceso(reclamoId, authentication);
        return estadoReclamoRepository.findByReclamoIdOrderByFechaAsc(reclamoId)
                .stream().map(EstadoReclamoResponse::de).toList();
    }

    @GetMapping("/reclamo/{reclamoId}/actual")
    public EstadoReclamoResponse actual(@PathVariable Long reclamoId, Authentication authentication) {
        validarAcceso(reclamoId, authentication);
        return estadoReclamoRepository.findFirstByReclamoIdOrderByFechaDescIdDesc(reclamoId)
                .map(EstadoReclamoResponse::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ese reclamo no tiene estados cargados"));
    }

    // Cambio de estado hecho por el municipio (SecurityConfig ya lo restringe).
    // Las reglas (transiciones, comentario obligatorio) y la notificacion al
    // vecino estan en ReclamoEstadoService.
    @PostMapping
    public ResponseEntity<EstadoReclamoResponse> crear(@Valid @RequestBody EstadoReclamoCrearRequest request) {
        EstadoReclamo guardado = reclamoEstadoService.cambiarEstado(
                request.reclamoId(), request.estado(), request.comentario());
        return ResponseEntity.status(HttpStatus.CREATED).body(EstadoReclamoResponse.de(guardado));
    }

    // Municipio: siempre puede. Vecino: solo si el reclamo es suyo.
    private void validarAcceso(Long reclamoId, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (usuario.getRol() == Rol.municipio) {
            return;
        }

        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));

        if (!reclamo.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ese reclamo no es tuyo");
        }
    }
}
