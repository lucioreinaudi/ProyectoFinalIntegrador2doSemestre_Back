package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Aca si va usuarioId: es el destinatario de la notificacion, no necesariamente
// quien esta logueado (por ejemplo, el sistema le avisa a un vecino que cambio
// el estado de SU reclamo).
public record NotificacionCrearRequest(
        @NotNull(message = "El usuario destinatario es obligatorio")
        Long usuarioId,

        @NotNull(message = "El reclamo es obligatorio")
        Long reclamoId,

        @NotBlank(message = "El mensaje es obligatorio")
        String mensaje
) {
}
