package com.example.demo.repositorio;

import com.example.demo.modelo.Alerta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Los EntityGraph traen zona y usuario en la misma consulta (AlertaResponse los
// usa; si no, seria una consulta extra por alerta). Mas nuevas primero.
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    @EntityGraph(attributePaths = {"zona", "usuario"})
    List<Alerta> findAllByOrderByFechaEmisionDescIdDesc();

    // estado: 'activa' o 'finalizada' (ver CHECK de la BD)
    @EntityGraph(attributePaths = {"zona", "usuario"})
    List<Alerta> findByEstadoOrderByFechaEmisionDescIdDesc(String estado);

    @EntityGraph(attributePaths = {"zona", "usuario"})
    List<Alerta> findByZonaIdAndEstadoOrderByFechaEmisionDescIdDesc(Long zonaId, String estado);
}
