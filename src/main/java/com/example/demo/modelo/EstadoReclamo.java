package com.example.demo.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "\"EstadoReclamo\"")
public class EstadoReclamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reclamo_id", nullable = false)
    private Reclamo reclamo;

    // Valores que acepta el CHECK de la BD: 'recibido', 'en revision', 'en proceso', 'resuelto'.
    // Es String (y no enum) porque "en revision" y "en proceso" tienen espacios. Default igual al de la BD.
    @Column(nullable = false, length = 20)
    private String estado = "recibido";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime fecha;

    @Column(columnDefinition = "text")
    private String comentario;

    public EstadoReclamo() {
    }

    public EstadoReclamo(Reclamo reclamo, String estado, String comentario) {
        this.reclamo = reclamo;
        this.estado = estado;
        this.comentario = comentario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Reclamo getReclamo() {
        return reclamo;
    }

    public void setReclamo(Reclamo reclamo) {
        this.reclamo = reclamo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFecha() {
        return fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
