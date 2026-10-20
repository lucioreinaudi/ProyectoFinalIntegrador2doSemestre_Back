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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/estados-reclamo")
public class EstadoReclamoController {

    // Mismos valores que el CHECK de la tabla EstadoReclamo en la BD.
    private static final Set<String> ESTADOS_VALIDOS = Set.of("recibido", "en revision", "en proceso", "resuelto", "rechazado");

    private final EstadoReclamoRepository estadoReclamoRepository;
    private final ReclamoRepository reclamoRepository;
    private final UsuarioRepository usuarioRepository;

    public EstadoReclamoController(EstadoReclamoRepository estadoReclamoRepository, ReclamoRepository reclamoRepository,
                                    UsuarioRepository usuarioRepository) {
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.reclamoRepository = reclamoRepository;
        this.usuarioRepository = usuarioRepository;
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

    @PostMapping
    public ResponseEntity<EstadoReclamoResponse> crear(@Valid @RequestBody EstadoReclamoCrearRequest request) {
        Reclamo reclamo = reclamoRepository.findById(request.reclamoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reclamo indicado"));

        String estado = request.estado() == null ? "recibido" : request.estado();
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado invalido. Valores permitidos: " + ESTADOS_VALIDOS);
        }

        EstadoReclamo nuevoEstado = new EstadoReclamo(reclamo, estado, request.comentario());
        EstadoReclamo guardado = estadoReclamoRepository.save(nuevoEstado);
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
