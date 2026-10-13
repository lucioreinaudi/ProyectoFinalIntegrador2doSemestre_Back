package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// No lleva usuarioId: el autor del reclamo es quien esta logueado (se toma del token),
// no un dato que mande el cliente.
public record ReclamoCrearRequest(
        @NotBlank(message = "El titulo es obligatorio")
        String titulo,

        String descripcion,
        String fotoUrl,
        String ubicacion,

        @NotNull(message = "La zona es obligatoria")
        Long zonaId,

        @NotNull(message = "La categoria es obligatoria")
        Long categoriaId
) {
}
