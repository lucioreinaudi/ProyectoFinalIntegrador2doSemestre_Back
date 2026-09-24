package com.example.demo.repositorio;

import com.example.demo.modelo.Alerta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // estado: 'activa' o 'finalizada' (ver CHECK de la BD)
    List<Alerta> findByEstado(String estado);
    List<Alerta> findByZonaIdAndEstado(Long zonaId, String estado);
}
