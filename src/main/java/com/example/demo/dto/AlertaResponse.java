package com.example.demo.dto;

import com.example.demo.modelo.Alerta;

import java.time.OffsetDateTime;

// cantidadReclamos: cuantos reclamos de vecinos estan vinculados ("Reportado por
// N vecinos" en lo publico, y origen "Desde reclamos (N)" / "Municipio" en el panel).
// Es solo un numero: nunca se exponen nombres ni datos de los vecinos (Q10, Q27).
// usuarioId/usuarioNombre son del autor, que siempre es el municipio.
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
        String usuarioNombre,
        long cantidadReclamos
) {
    public static AlertaResponse de(Alerta a, long cantidadReclamos) {
        return new AlertaResponse(
                a.getId(), a.getTitulo(), a.getDescripcion(), a.getNivelGravedad(), a.getRecomendaciones(),
                a.getTipo(), a.getImagenUrl(),
                a.getFechaEmision(), a.getEstado(),
                a.getZona().getId(), a.getZona().getNombre(),
                a.getUsuario().getId(), a.getUsuario().getNombre(),
                cantidadReclamos
        );
    }
}
