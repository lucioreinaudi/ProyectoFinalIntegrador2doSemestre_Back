package com.example.demo.dto;

// Lo que devuelve el login: los datos del usuario, mas el token de sesion.
public record LoginResponse(
        UsuarioResponse usuario,
        String token
) {
}
