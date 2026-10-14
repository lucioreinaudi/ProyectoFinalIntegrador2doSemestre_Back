package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

// Lo que se recibe para editar el propio perfil (PUT /api/usuarios/me).
// A proposito NO tiene email ni rol: esos no se pueden cambiar desde aca.
// Tampoco tiene password: eso es un endpoint aparte (PUT /api/usuarios/me/password).
public record UsuarioActualizarRequest(
        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String apellido,
        String documento,
        String direccion,
        LocalDate fechaNacimiento,
        String telefono,
        Long zonaId
) {
}