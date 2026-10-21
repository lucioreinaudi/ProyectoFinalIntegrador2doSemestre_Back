package com.example.demo.dto;

import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;

import java.time.OffsetDateTime;

// Fila de la pestana Usuarios del panel (Q13): solo lectura, sin passwordHash.
public record UsuarioAdminResponse(
        Long id,
        String nombre,
        String apellido,
        String documento,
        String email,
        String telefono,
        Long zonaId,
        String zonaNombre,
        Rol rol,
        OffsetDateTime fechaRegistro,
        long cantidadReclamos
) {
    public static UsuarioAdminResponse de(Usuario u, long cantidadReclamos) {
        return new UsuarioAdminResponse(
                u.getId(), u.getNombre(), u.getApellido(), u.getDocumento(), u.getEmail(), u.getTelefono(),
                u.getZona() != null ? u.getZona().getId() : null,
                u.getZona() != null ? u.getZona().getNombre() : null,
                u.getRol(), u.getFechaRegistro(), cantidadReclamos
        );
    }
}
