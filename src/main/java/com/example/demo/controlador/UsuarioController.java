package com.example.demo.controlador;

import com.example.demo.dto.CambiarPasswordRequest;
import com.example.demo.dto.UsuarioActualizarRequest;
import com.example.demo.dto.UsuarioCrearRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.modelo.Usuario;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.repositorio.ZonaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final ZonaRepository zonaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, ZonaRepository zonaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.zonaRepository = zonaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::de)
                .toList();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        // La validacion de formato (campos obligatorios, email valido, largo de password)
        // la hace @Valid sobre UsuarioCrearRequest. Lo que queda aca es lo que un DTO
        // no puede validar solo: que el email no este repetido, y que la zona exista.
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email");
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(request.email());
        usuario.setNombre(request.nombre());
        usuario.setPasswordHash(passwordEncoder.encode(request.password()));
        // No se setea rol: queda el default de la entidad (vecino). Este endpoint
        // es publico, asi que nunca puede crear un usuario municipio.
        usuario.setTelefono(request.telefono());
        usuario.setApellido(request.apellido());
        usuario.setDocumento(request.documento());
        usuario.setDireccion(request.direccion());
        usuario.setFechaNacimiento(request.fechaNacimiento());

        if (request.zonaId() != null) {
            Zona zona = zonaRepository.findById(request.zonaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));
            usuario.setZona(zona);
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.de(guardado));
    }

    // Edita el PROPIO perfil. El usuario sale del token (como en Reclamo/Alerta),
    // nunca de un id en la URL: nadie puede editar a otro. A proposito no se puede
    // cambiar email ni rol desde aca (UsuarioActualizarRequest no los tiene), y la
    // contraseÃ±a tiene su propio endpoint (PUT /api/usuarios/me/password).
    @PutMapping("/me")
    public UsuarioResponse actualizarPerfil(@Valid @RequestBody UsuarioActualizarRequest request, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        usuario.setNombre(request.nombre());
        usuario.setApellido(request.apellido());
        usuario.setDocumento(request.documento());
        usuario.setDireccion(request.direccion());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setTelefono(request.telefono());

        if (request.zonaId() != null) {
            Zona zona = zonaRepository.findById(request.zonaId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));
            usuario.setZona(zona);
        } else {
            usuario.setZona(null);
        }

        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    // Cambia la propia contraseña. Exige la actual (verificada con BCrypt) antes
    // de aceptar la nueva, para que alguien que te robe la sesion (o el token)
    // no pueda cambiarte la contraseña sin saber la de antes.
    @PutMapping("/me/password")
    public ResponseEntity<Void> cambiarPassword(@Valid @RequestBody CambiarPasswordRequest request, Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.passwordNueva()));
        usuarioRepository.save(usuario);
        return ResponseEntity.noContent().build();
    }
}