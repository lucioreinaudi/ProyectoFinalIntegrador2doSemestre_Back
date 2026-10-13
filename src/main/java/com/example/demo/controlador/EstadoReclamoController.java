package com.example.demo.controlador;

import com.example.demo.dto.EstadoReclamoCrearRequest;
import com.example.demo.dto.EstadoReclamoResponse;
import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Reclamo;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.ReclamoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/estados-reclamo")
public class EstadoReclamoController {

    // Mismos valores que el CHECK de la tabla EstadoReclamo en la BD.
    private static final Set<String> ESTADOS_VALIDOS = Set.of("recibido", "en revision", "en proceso", "resuelto");

    private final EstadoReclamoRepository estadoReclamoRepository;
    private final ReclamoRepository reclamoRepository;

    public EstadoReclamoController(EstadoReclamoRepository estadoReclamoRepository, ReclamoRepository reclamoRepository) {
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.reclamoRepository = reclamoRepository;
    }

    @GetMapping("/reclamo/{reclamoId}")
    public List<EstadoReclamoResponse> historial(@PathVariable Long reclamoId) {
        return estadoReclamoRepository.findByReclamoIdOrderByFechaAsc(reclamoId)
                .stream().map(EstadoReclamoResponse::de).toList();
    }

    @GetMapping("/reclamo/{reclamoId}/actual")
    public EstadoReclamoResponse actual(@PathVariable Long reclamoId) {
        return estadoReclamoRepository.findFirstByReclamoIdOrderByFechaDescIdDesc(reclamoId)
                .map(EstadoReclamoResponse::de)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ese reclamo no tiene estados cargados"));
    }

    @PostMapping
    public ResponseEntity<EstadoReclamoResponse> crear(@Valid @RequestBody EstadoReclamoCrearRequest request) {
        Reclamo reclamo = reclamoRepository.findById(request.reclamoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reclamo indicado"));

        String estado = request.estado() == null ? "recibido" : request.estado();
        if (!ESTADOS_VALIDOS.contains(estado)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado invalido. Valores permitidos: " + ESTADOS_VALIDOS);
        }

        EstadoReclamo nuevoEstado = new EstadoReclamo(reclamo, estado, request.comentario());
        EstadoReclamo guardado = estadoReclamoRepository.save(nuevoEstado);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstadoReclamoResponse.de(guardado));
    }
}
