package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

// Baja logica / reactivacion de una zona o categoria (PATCH .../{id}/activa).
public record ActivaRequest(
        @NotNull(message = "Indica activa: true o false")
        Boolean activa
) {
}
