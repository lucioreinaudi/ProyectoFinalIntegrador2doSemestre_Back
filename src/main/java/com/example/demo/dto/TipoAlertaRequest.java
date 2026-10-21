package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Alta y edicion de un tipo de alerta. La clave no viene: la genera el back.
// imagenUrl: opcional, tiene que ser de nuestro Storage (POST /api/admin/alertas/imagen).
public record TipoAlertaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre puede tener hasta 60 caracteres")
        String nombre,

        String icono,

        String imagenUrl
) {
}
