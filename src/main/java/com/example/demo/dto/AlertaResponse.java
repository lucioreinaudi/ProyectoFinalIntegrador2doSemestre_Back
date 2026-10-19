package com.example.demo.dto;

import com.example.demo.modelo.Alerta;

import java.time.OffsetDateTime;

public record AlertaResponse(
        Long id,
        String titulo,
        String descripcion,
        String nivelGravedad,
        String recomendaciones,
        String tipo,
        String imagenUrl,
        OffsetDateTime fechaEmision,
        String estado,
        Long zonaId,
        String zonaNombre,
        Long usuarioId,
        String usuarioNombre
) {
    public static AlertaResponse de(Alerta a) {
        return new AlertaResponse(
                a.getId(), a.getTitulo(), a.getDescripcion(), a.getNivelGravedad(), a.getRecomendaciones(),
                a.getTipo(), a.getImagenUrl(),
                a.getFechaEmision(), a.getEstado(),
                a.getZona().getId(), a.getZona().getNombre(),
                a.getUsuario().getId(), a.getUsuario().getNombre()
        );
    }
}
