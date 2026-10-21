package com.example.demo.repositorio;

import com.example.demo.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombre(String nombre);

    // Para no repetir nombres sin importar mayusculas ("Otro" y "otro").
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    // Las que ven los vecinos (Q16).
    List<Categoria> findByActivaTrueOrderByNombreAsc();

    // Todas, para el panel (Q29: las dadas de baja siguen en los filtros).
    List<Categoria> findAllByOrderByNombreAsc();
}
