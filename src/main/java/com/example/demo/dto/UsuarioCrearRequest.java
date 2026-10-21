package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Lo que se recibe para crear un usuario.
// telefono, apellido, documento, direccion, fechaNacimiento y zonaId son opcionales.
// A proposito NO tiene "rol": todo usuario que se registra por este endpoint publico
// es vecino, sin excepcion. Si algun cliente manda "rol" en el body, Jackson lo
// ignora (no esta declarado aca), no llega a tener ningun efecto.
// OJO: "password" se hashea con BCrypt en el controller antes de guardarla.
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

        String telefono,
        Long zonaId
) {
}