package com.example.demo.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

// Tabla intermedia N:M entre Alerta y Reclamo (con la columna extra fecha_vinculacion).
@Entity
@Table(name = "\"AlertaReclamo\"")
public class AlertaReclamo {

    @EmbeddedId
    private AlertaReclamoId id = new AlertaReclamoId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("alertaId")
    @JoinColumn(name = "alerta_id")
    private Alerta alerta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("reclamoId")
    @JoinColumn(name = "reclamo_id")
    private Reclamo reclamo;

    @CreationTimestamp
    @Column(name = "fecha_vinculacion", nullable = false, updatable = false)
    private OffsetDateTime fechaVinculacion;

    public AlertaReclamo() {
    }

    public AlertaReclamo(Alerta alerta, Reclamo reclamo) {
        this.alerta = alerta;
        this.reclamo = reclamo;
        this.id = new AlertaReclamoId(alerta.getId(), reclamo.getId());
    }

    public AlertaReclamoId getId() {
        return id;
    }

    public void setId(AlertaReclamoId id) {
        this.id = id;
    }

    public Alerta getAlerta() {
        return alerta;
    }

    public void setAlerta(Alerta alerta) {
        this.alerta = alerta;
    }

    public Reclamo getReclamo() {
        return reclamo;
    }

    public void setReclamo(Reclamo reclamo) {
        this.reclamo = reclamo;
    }

    public OffsetDateTime getFechaVinculacion() {
        return fechaVinculacion;
    }
}
