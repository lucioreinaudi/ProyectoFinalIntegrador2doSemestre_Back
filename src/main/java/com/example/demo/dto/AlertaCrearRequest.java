package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Datos de una alerta, para crearla (POST /api/alertas), editarla (PUT /api/alertas/{id})
// o crearla al publicar un reclamo. No lleva usuarioId: quien emite la alerta es
// quien esta logueado (se toma del token).
public record AlertaCrearRequest(
        @NotBlank(message = "El titulo es obligatorio")
        @Size(max = 150, message = "El titulo puede tener hasta 150 caracteres")
        String titulo,

        @NotBlank(message = "La descripcion es obligatoria")
        String descripcion,

        // alto | medio | bajo (los valida AlertaService).
        @NotBlank(message = "El nivel de gravedad es obligatorio")
        String nivelGravedad,

        String recomendaciones,

        // Opcional: si no viene, la alerta queda con tipo 'otro'.
        String tipo,

        // Opcional.
        String imagenUrl,

        @NotNull(message = "La zona es obligatoria")
        Long zonaId
) {
}
