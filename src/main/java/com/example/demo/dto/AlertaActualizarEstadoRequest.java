package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record AlertaActualizarEstadoRequest(
        @NotBlank(message = "El estado es obligatorio")
        String estado
) {
}
