package com.example.demo.controlador;

import com.example.demo.JwtService;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
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

// Login: confirma que el email y password son correctos, devuelve los datos del usuario
// y un token JWT. OJO: por ahora el token se EMITE pero ningun endpoint lo exige todavia
// (esa es la proxima card: validar el JWT en los endpoints protegidos).
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
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

        Usuario encontrado = usuario.get();
        String token = jwtService.generarToken(encontrado.getEmail());
        return new LoginResponse(UsuarioResponse.de(encontrado), token);
    }
}
