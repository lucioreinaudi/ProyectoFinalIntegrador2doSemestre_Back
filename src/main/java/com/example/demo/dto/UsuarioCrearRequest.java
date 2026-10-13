package com.example.demo.dto;

import com.example.demo.modelo.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Lo que se recibe para crear un usuario.
// rol, telefono, apellido, documento, direccion, fechaNacimiento y zonaId son opcionales.
// OJO: "password" se guarda hoy tal cual se la hashea con BCrypt en el controller.
public record UsuarioCrearRequest(
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato valido")
        String email,

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        String apellido,
        String documento,
        String direccion,
        LocalDate fechaNacimiento,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        Rol rol,
        String telefono,
        Long zonaId
) {
}
