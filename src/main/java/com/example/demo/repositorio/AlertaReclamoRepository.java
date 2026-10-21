package com.example.demo.repositorio;

import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.AlertaReclamoId;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface AlertaReclamoRepository extends JpaRepository<AlertaReclamo, AlertaReclamoId> {
    List<AlertaReclamo> findByAlertaId(Long alertaId);
    List<AlertaReclamo> findByReclamoId(Long reclamoId);

    // De estos reclamos, cuales estan publicados (vinculados a alguna alerta).
    @Query("select distinct ar.reclamo.id from AlertaReclamo ar where ar.reclamo.id in :reclamoIds")
    Set<Long> findReclamoIdsPublicados(@Param("reclamoIds") Collection<Long> reclamoIds);

    // Cantidad de reclamos vinculados a cada alerta, para varias alertas en una consulta.
    // Devuelve pares [alertaId, cantidad]; las alertas sin vinculos no aparecen.
    @Query("select ar.alerta.id, count(ar) from AlertaReclamo ar where ar.alerta.id in :alertaIds group by ar.alerta.id")
    List<Object[]> contarPorAlerta(@Param("alertaIds") Collection<Long> alertaIds);

    long countByAlertaId(Long alertaId);

    boolean existsByAlertaId(Long alertaId);

    // Para el detalle: los vinculos de un reclamo con su alerta ya cargada.
    @EntityGraph(attributePaths = {"alerta"})
    List<AlertaReclamo> findByReclamoIdOrderByFechaVinculacionAsc(Long reclamoId);
}
