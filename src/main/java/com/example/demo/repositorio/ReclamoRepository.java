package com.example.demo.repositorio;

import com.example.demo.modelo.Reclamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

// JpaSpecificationExecutor: para armar el listado del panel con filtros opcionales.
public interface ReclamoRepository extends JpaRepository<Reclamo, Long>, JpaSpecificationExecutor<Reclamo> {
    List<Reclamo> findByUsuarioId(Long usuarioId);

    // Cantidad de reclamos de cada usuario, para varios usuarios en una sola consulta.
    // Devuelve pares [usuarioId, cantidad]; los que no tienen reclamos no aparecen.
    @Query("select r.usuario.id, count(r) from Reclamo r where r.usuario.id in :usuarioIds group by r.usuario.id")
    List<Object[]> contarPorUsuario(@Param("usuarioIds") Collection<Long> usuarioIds);

    // Mis Reportes: los del vecino, del mas nuevo al mas viejo. El EntityGraph trae
    // zona, categoria y usuario en la misma consulta (si no, seria una consulta
    // extra por reclamo para cada uno).
    @EntityGraph(attributePaths = {"zona", "categoria", "usuario"})
    List<Reclamo> findByUsuarioIdOrderByFechaCreacionDescIdDesc(Long usuarioId);

    // Listado del panel (filtros + paginacion), con el mismo EntityGraph.
    @Override
    @EntityGraph(attributePaths = {"zona", "categoria", "usuario"})
    Page<Reclamo> findAll(Specification<Reclamo> spec, Pageable pageable);
    List<Reclamo> findByZonaId(Long zonaId);
    List<Reclamo> findByCategoriaId(Long categoriaId);
}
