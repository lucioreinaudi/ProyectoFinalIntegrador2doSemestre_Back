package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EstadoReclamoCrearRequest(
        @NotNull(message = "El reclamo es obligatorio")
        Long reclamoId,

        // Obligatorio: antes, si faltaba, se tomaba "recibido", que con la tabla de
        // transiciones nunca es un destino valido.
        @NotBlank(message = "El estado es obligatorio")
        String estado,
        String comentario
) {
}
