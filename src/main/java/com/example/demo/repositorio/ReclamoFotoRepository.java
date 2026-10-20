package com.example.demo.repositorio;

import com.example.demo.modelo.ReclamoFoto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReclamoFotoRepository extends JpaRepository<ReclamoFoto, Long> {
    List<ReclamoFoto> findByReclamoIdOrderByOrdenAscIdAsc(Long reclamoId);

    // Para armar listados sin una consulta por reclamo.
    List<ReclamoFoto> findByReclamoIdInOrderByOrdenAscIdAsc(Collection<Long> reclamoIds);

    long countByReclamoId(Long reclamoId);
}
