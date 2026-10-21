package com.example.demo.repositorio;

import com.example.demo.modelo.Zona;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ZonaRepository extends JpaRepository<Zona, Long> {
    Optional<Zona> findByNombre(String nombre);

    // Para no repetir nombres sin importar mayusculas ("Centro" y "centro").
    Optional<Zona> findByNombreIgnoreCase(String nombre);

    // Las que ven los vecinos (Q16).
    List<Zona> findByActivaTrueOrderByNombreAsc();

    // Todas, para el panel (Q29: las dadas de baja siguen en los filtros).
    List<Zona> findAllByOrderByNombreAsc();
}
