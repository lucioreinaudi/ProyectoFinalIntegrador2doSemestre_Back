package com.example.demo.dto;

import com.example.demo.modelo.Rol;

import java.time.LocalDate;

// Lo que se recibe para crear un usuario.
// rol, telefono, apellido, documento, direccion, fechaNacimiento y zonaId son opcionales.
// OJO: "password" se guarda hoy tal cual en passwordHash, SIN hashear.
// Falta agregar spring-boot-starter-security y usar BCrypt antes de usar esto en serio.
public record UsuarioCrearRequest(
        String email,
        String nombre,
        String apellido,
        String documento,
        String direccion,
        LocalDate fechaNacimiento,
        String password,
        Rol rol,
        String telefono,
        Long zonaId
) {
}