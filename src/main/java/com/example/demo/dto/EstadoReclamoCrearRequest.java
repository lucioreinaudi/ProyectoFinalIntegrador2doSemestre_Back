package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoReclamoCrearRequest(
        @NotNull(message = "El reclamo es obligatorio")
        Long reclamoId,

        String estado,
        String comentario
) {
}
