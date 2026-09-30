package com.example.demo.controlador;

import com.example.demo.dto.UsuarioCrearRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestBody UsuarioCrearRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.nombre() == null || request.nombre().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email, nombre y password son obligatorios");
        }
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setNombre(request.nombre());
        // TODO: hashear con BCrypt cuando se agregue spring-boot-starter-security.
        usuario.setPasswordHash(request.password());
        if (request.rol() != null) {
            usuario.setRol(request.rol());
        }
        usuario.setTelefono(request.telefono());
        usuario.setApellido(request.apellido());
        usuario.setDocumento(request.documento());
        usuario.setDireccion(request.direccion());

        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(guardado));
    }
}
