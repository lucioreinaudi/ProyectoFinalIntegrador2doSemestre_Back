package com.example.demo.repositorio;

import com.example.demo.modelo.EstadoReclamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EstadoReclamoRepository extends JpaRepository<EstadoReclamo, Long> {
    // Historial completo de un reclamo, del mas viejo al mas nuevo.
    List<EstadoReclamo> findByReclamoIdOrderByFechaAsc(Long reclamoId);

    // Igual, pero desempatando por id: el ultimo es siempre el estado actual
    // (mismo criterio que findFirstByReclamoIdOrderByFechaDescIdDesc).
    List<EstadoReclamo> findByReclamoIdOrderByFechaAscIdAsc(Long reclamoId);

    // Estado actual de un reclamo = el registro mas reciente (el id desempata si coinciden las fechas).
    Optional<EstadoReclamo> findFirstByReclamoIdOrderByFechaDescIdDesc(Long reclamoId);

    // Historiales de varios reclamos juntos, del mas nuevo al mas viejo: el primero
    // de cada reclamo es su estado actual. Una sola consulta para toda la pagina.
    List<EstadoReclamo> findByReclamoIdInOrderByFechaDescIdDesc(Collection<Long> reclamoIds);
}
