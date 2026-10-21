package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Alta y edicion de una zona (POST /api/zonas, PUT /api/zonas/{id}).
public record ZonaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre puede tener hasta 100 caracteres")
        String nombre,

        String descripcion
) {
}
