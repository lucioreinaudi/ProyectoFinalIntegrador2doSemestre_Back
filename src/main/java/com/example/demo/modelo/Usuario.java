package com.example.demo.modelo;

import jakarta.persistence.*;

@Entity
@Table(name = "\"Usuario\"")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Valores iguales a los de la BD ('vecino' / 'municipio'). Default igual al de la BD.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.vecino;

    @Column(length = 30)
    private String telefono;

    public Usuario() {
    }

    public Usuario(String email, String nombre, String passwordHash, Rol rol) {
        this.email = email;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}