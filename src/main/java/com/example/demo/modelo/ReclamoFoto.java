package com.example.demo.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Una foto de un reclamo: el archivo vive en Supabase Storage, aca solo su URL.
@Entity
@Table(name = "\"Reclamo_Foto\"")
public class ReclamoFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_foto")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reclamo_id", nullable = false)
    private Reclamo reclamo;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(nullable = false)
    private short orden;

    @CreationTimestamp
    @Column(name = "fecha_subida", nullable = false, updatable = false)
    private OffsetDateTime fechaSubida;

    public ReclamoFoto() {
    }

    public ReclamoFoto(Reclamo reclamo, String url, short orden) {
        this.reclamo = reclamo;
        this.url = url;
        this.orden = orden;
    }

    public Long getId() {
        return id;
    }

    public Reclamo getReclamo() {
        return reclamo;
    }

    public String getUrl() {
        return url;
    }

    public short getOrden() {
        return orden;
    }

    public OffsetDateTime getFechaSubida() {
        return fechaSubida;
    }
}
