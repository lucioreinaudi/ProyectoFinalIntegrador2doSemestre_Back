package com.example.demo.repositorio;

import com.example.demo.modelo.Reclamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReclamoRepository extends JpaRepository<Reclamo, Long> {
    List<Reclamo> findByUsuarioId(Long usuarioId);
    List<Reclamo> findByZonaId(Long zonaId);
    List<Reclamo> findByCategoriaId(Long categoriaId);
}
