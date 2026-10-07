package com.example.demo.dto;

// Lo que se recibe para iniciar sesion.
public record LoginRequest(
        String email,
        String password
) {
}
