package com.example.demo.controlador;

import com.example.demo.dto.ZonaRequest;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.ZonaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Zona no devuelve passwordHash ni nada sensible, asi que se puede usar la entidad
// directa como respuesta, sin necesidad de un DTO de salida.
@RestController
@RequestMapping("/api/zonas")
public class ZonaController {

    private final ZonaRepository zonaRepository;

    public ZonaController(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    @GetMapping
    public List<Zona> listar() {
        return zonaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Zona obtener(@PathVariable Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa zona"));
    }

    @PostMapping
    public ResponseEntity<Zona> crear(@RequestBody ZonaRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (zonaRepository.findByNombre(request.nombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una zona con ese nombre");
        }
        Zona zona = new Zona(request.nombre(), request.descripcion());
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaRepository.save(zona));
    }
}
