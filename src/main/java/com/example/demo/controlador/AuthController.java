package com.example.demo.controlador;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.UsuarioResponse;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

// Login simple: confirma que el email y password son correctos y devuelve los datos del usuario.
// IMPORTANTE: esto NO es un login real todavia. No genera token ni sesion, asi que ningun otro
// endpoint sabe "quien esta logueado". Falta agregar JWT (u otro mecanismo) para eso.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public UsuarioResponse login(@RequestBody LoginRequest request) {
        if (request.email() == null || request.email().isBlank()
                || request.password() == null || request.password().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email y password son obligatorios");
        }

        Optional<Usuario> usuario = usuarioRepository.findByEmail(request.email());

        // A proposito no se distingue "no existe el email" de "la contraseña es incorrecta":
        // decirlo por separado facilitaria que alguien adivine que emails estan registrados.
        if (usuario.isEmpty() || !passwordEncoder.matches(request.password(), usuario.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
        }

        return UsuarioResponse.de(usuario.get());
    }
}
