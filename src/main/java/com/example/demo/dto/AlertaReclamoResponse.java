package com.example.demo.dto;

import com.example.demo.modelo.AlertaReclamo;

import java.time.OffsetDateTime;

public record AlertaReclamoResponse(
        Long alertaId,
        Long reclamoId,
        OffsetDateTime fechaVinculacion
) {
    public static AlertaReclamoResponse de(AlertaReclamo ar) {
        return new AlertaReclamoResponse(ar.getAlerta().getId(), ar.getReclamo().getId(), ar.getFechaVinculacion());
    }
}
