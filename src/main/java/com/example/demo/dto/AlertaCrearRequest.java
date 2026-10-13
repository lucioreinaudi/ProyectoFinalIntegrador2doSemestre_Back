package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// No lleva usuarioId: quien emite la alerta es quien esta logueado (se toma del token).
public record AlertaCrearRequest(
        @NotBlank(message = "El titulo es obligatorio")
        String titulo,

        String descripcion,

        @NotBlank(message = "El nivel de gravedad es obligatorio")
        String nivelGravedad,

        String recomendaciones,

        @NotNull(message = "La zona es obligatoria")
        Long zonaId
) {
}
