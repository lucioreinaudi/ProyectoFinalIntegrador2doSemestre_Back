package com.example.demo.dto;

import com.example.demo.modelo.Rol;

// Lo que se recibe para crear un usuario. rol y telefono son opcionales.
// OJO: "password" se guarda hoy tal cual en passwordHash, SIN hashear.
// Falta agregar spring-boot-starter-security y usar BCrypt antes de usar esto en serio.
public record UsuarioCrearRequest(
        String email,
        String nombre,
        String password,
        Rol rol,
        String telefono
) {
}
