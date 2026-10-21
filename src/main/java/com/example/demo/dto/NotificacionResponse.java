package com.example.demo.dto;

import com.example.demo.modelo.Notificacion;

import java.time.OffsetDateTime;

public record NotificacionResponse(
        Long id,
        Long usuarioId,
        Long reclamoId,
        String mensaje,
        OffsetDateTime fecha,
        boolean leida
) {
    public static NotificacionResponse de(Notificacion n) {
        return new NotificacionResponse(
                n.getId(), n.getUsuario().getId(), n.getReclamo().getId(), n.getMensaje(), n.getFecha(), n.isLeida()
        );
    }
}
