package com.example.demo.dto;

import com.example.demo.modelo.Reclamo;

import java.time.OffsetDateTime;

// No incluye el objeto Usuario completo (filtraria passwordHash via Jackson),
// solo id y nombre del autor.
public record ReclamoResponse(
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
        String usuarioNombre
) {
    public static ReclamoResponse de(Reclamo r) {
        return new ReclamoResponse(
                r.getId(), r.getTitulo(), r.getDescripcion(), r.getFotoUrl(), r.getUbicacion(),
                r.getFechaCreacion(),
                r.getZona().getId(), r.getZona().getNombre(),
                r.getCategoria().getId(), r.getCategoria().getNombre(),
                r.getUsuario().getId(), r.getUsuario().getNombre()
        );
    }
}
