package com.example.demo.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

// Clave primaria compuesta de "AlertaReclamo": (alerta_id, reclamo_id).
@Embeddable
public class AlertaReclamoId implements Serializable {

    @Column(name = "alerta_id")
    private Long alertaId;

    @Column(name = "reclamo_id")
    private Long reclamoId;

    public AlertaReclamoId() {
    }

    public AlertaReclamoId(Long alertaId, Long reclamoId) {
        this.alertaId = alertaId;
        this.reclamoId = reclamoId;
    }

    public Long getAlertaId() {
        return alertaId;
    }

    public void setAlertaId(Long alertaId) {
        this.alertaId = alertaId;
    }

    public Long getReclamoId() {
        return reclamoId;
    }

    public void setReclamoId(Long reclamoId) {
        this.reclamoId = reclamoId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertaReclamoId that)) return false;
        return Objects.equals(alertaId, that.alertaId) && Objects.equals(reclamoId, that.reclamoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertaId, reclamoId);
    }
}
