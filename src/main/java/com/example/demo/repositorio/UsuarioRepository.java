package com.example.demo.repositorio;

import com.example.demo.modelo.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

// JpaSpecificationExecutor: para el listado del panel con filtros opcionales.
public interface UsuarioRepository extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
    Optional<Usuario> findByEmail(String email);

    // Listado del panel: trae la zona en la misma consulta (si no, una extra por usuario).
    @Override
    @EntityGraph(attributePaths = {"zona"})
    Page<Usuario> findAll(Specification<Usuario> spec, Pageable pageable);
}
