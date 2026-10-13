package com.example.demo.dto;

import com.example.demo.modelo.EstadoReclamo;

import java.time.OffsetDateTime;

public record EstadoReclamoResponse(
        Long id,
        Long reclamoId,
        String estado,
        OffsetDateTime fecha,
        String comentario
) {
    public static EstadoReclamoResponse de(EstadoReclamo e) {
        return new EstadoReclamoResponse(e.getId(), e.getReclamo().getId(), e.getEstado(), e.getFecha(), e.getComentario());
    }
}
