package com.example.demo.dto;

import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Reclamo;

import java.time.OffsetDateTime;

// Reclamo con su estado actual y si esta publicado. Lo usan el listado del panel
// y Mis Reportes (GET /api/reclamos/me). Tiene los mismos campos que
// ReclamoResponse, mas los nuevos al final, para no romper al front.
public record ReclamoResumenResponse(
        Long id,
        String titulo,
        String descripcion,
        String fotoUrl,
        String ubicacion,
        OffsetDateTime fechaCreacion,
        Long zonaId,
        String zonaNombre,
        Long categoriaId,
        String categoriaNombre,
        Long usuarioId,
        String usuarioNombre,
        String estadoActual,
        OffsetDateTime fechaEstadoActual,
        boolean publicado
) {
    // estadoActual puede ser null en un reclamo viejo sin historial.
    public static ReclamoResumenResponse de(Reclamo r, EstadoReclamo estadoActual, boolean publicado) {
        return new ReclamoResumenResponse(
                r.getId(), r.getTitulo(), r.getDescripcion(), r.getFotoUrl(), r.getUbicacion(),
                r.getFechaCreacion(),
                r.getZona().getId(), r.getZona().getNombre(),
                r.getCategoria().getId(), r.getCategoria().getNombre(),
                r.getUsuario().getId(), r.getUsuario().getNombre(),
                estadoActual != null ? estadoActual.getEstado() : null,
                estadoActual != null ? estadoActual.getFecha() : null,
                publicado
        );
    }
}
