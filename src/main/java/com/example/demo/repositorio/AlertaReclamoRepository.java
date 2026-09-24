package com.example.demo.repositorio;

import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.AlertaReclamoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertaReclamoRepository extends JpaRepository<AlertaReclamo, AlertaReclamoId> {
    List<AlertaReclamo> findByAlertaId(Long alertaId);
    List<AlertaReclamo> findByReclamoId(Long reclamoId);
}
