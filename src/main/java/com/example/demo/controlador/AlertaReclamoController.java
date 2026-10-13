package com.example.demo.controlador;

import com.example.demo.dto.AlertaReclamoCrearRequest;
import com.example.demo.dto.AlertaReclamoResponse;
import com.example.demo.modelo.Alerta;
import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.AlertaReclamoId;
import com.example.demo.modelo.Reclamo;
import com.example.demo.repositorio.AlertaReclamoRepository;
import com.example.demo.repositorio.AlertaRepository;
import com.example.demo.repositorio.ReclamoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/alertas-reclamos")
public class AlertaReclamoController {

    private final AlertaReclamoRepository alertaReclamoRepository;
    private final AlertaRepository alertaRepository;
    private final ReclamoRepository reclamoRepository;

    public AlertaReclamoController(AlertaReclamoRepository alertaReclamoRepository, AlertaRepository alertaRepository,
                                    ReclamoRepository reclamoRepository) {
        this.alertaReclamoRepository = alertaReclamoRepository;
        this.alertaRepository = alertaRepository;
        this.reclamoRepository = reclamoRepository;
    }

    @GetMapping("/alerta/{alertaId}")
    public List<AlertaReclamoResponse> porAlerta(@PathVariable Long alertaId) {
        return alertaReclamoRepository.findByAlertaId(alertaId).stream().map(AlertaReclamoResponse::de).toList();
    }

    @GetMapping("/reclamo/{reclamoId}")
    public List<AlertaReclamoResponse> porReclamo(@PathVariable Long reclamoId) {
        return alertaReclamoRepository.findByReclamoId(reclamoId).stream().map(AlertaReclamoResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<AlertaReclamoResponse> vincular(@Valid @RequestBody AlertaReclamoCrearRequest request) {
        Alerta alerta = alertaRepository.findById(request.alertaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la alerta indicada"));
        Reclamo reclamo = reclamoRepository.findById(request.reclamoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reclamo indicado"));

        AlertaReclamoId id = new AlertaReclamoId(alerta.getId(), reclamo.getId());
        if (alertaReclamoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esa alerta ya esta vinculada a ese reclamo");
        }

        AlertaReclamo vinculo = new AlertaReclamo(alerta, reclamo);
        AlertaReclamo guardado = alertaReclamoRepository.save(vinculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(AlertaReclamoResponse.de(guardado));
    }

    @DeleteMapping("/{alertaId}/{reclamoId}")
    public ResponseEntity<Void> desvincular(@PathVariable Long alertaId, @PathVariable Long reclamoId) {
        AlertaReclamoId id = new AlertaReclamoId(alertaId, reclamoId);
        if (!alertaReclamoRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ese vinculo no existe");
        }
        alertaReclamoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
