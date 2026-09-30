package com.example.demo.dto;

import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;

// Lo que devuelve la API. A proposito no incluye passwordHash.
public record UsuarioResponse(
        Long id,
        String email,
        String nombre,
        String apellido,
        String documento,
        String direccion,
        Rol rol,
        String telefono
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getEmail(), u.getNombre(), u.getApellido(),
                u.getDocumento(), u.getDireccion(), u.getRol(), u.getTelefono()
        );
    }
}