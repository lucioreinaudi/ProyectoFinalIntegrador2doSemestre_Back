package com.example.demo.repositorio;

import com.example.demo.modelo.EstadoReclamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoReclamoRepository extends JpaRepository<EstadoReclamo, Long> {
    // Historial completo de un reclamo, del mas viejo al mas nuevo.
    List<EstadoReclamo> findByReclamoIdOrderByFechaAsc(Long reclamoId);

    // Estado actual de un reclamo = el registro mas reciente (el id desempata si coinciden las fechas).
    Optional<EstadoReclamo> findFirstByReclamoIdOrderByFechaDescIdDesc(Long reclamoId);
}
