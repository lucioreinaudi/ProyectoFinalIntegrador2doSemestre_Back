package com.example.demo.controlador;

import com.example.demo.dto.ActivaRequest;
import com.example.demo.dto.TipoAlertaRequest;
import com.example.demo.modelo.TipoAlerta;
import com.example.demo.service.TipoAlertaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Tipos de alerta. El GET es publico y devuelve TODOS (con "activa"): el Home los
// necesita para mostrar el icono y la imagen de referencia de cada alerta, aun si
// su tipo ya se dio de baja. Los formularios ofrecen solo los activos.
// Crear, editar y dar de baja: solo municipio (SecurityConfig).
@RestController
@RequestMapping("/api/tipos-alerta")
public class TipoAlertaController {

    private final TipoAlertaService tipoAlertaService;

    public TipoAlertaController(TipoAlertaService tipoAlertaService) {
        this.tipoAlertaService = tipoAlertaService;
    }

    @GetMapping
    public List<TipoAlerta> listar() {
        return tipoAlertaService.listar();
    }

    @GetMapping("/{id}")
    public TipoAlerta obtener(@PathVariable Long id) {
        return tipoAlertaService.buscarOFallar(id);
    }

    @PostMapping
    public ResponseEntity<TipoAlerta> crear(@Valid @RequestBody TipoAlertaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tipoAlertaService.crear(request));
    }

    @PutMapping("/{id}")
    public TipoAlerta editar(@PathVariable Long id, @Valid @RequestBody TipoAlertaRequest request) {
        return tipoAlertaService.editar(id, request);
    }

    @PatchMapping("/{id}/activa")
    public TipoAlerta cambiarActiva(@PathVariable Long id, @Valid @RequestBody ActivaRequest request) {
        return tipoAlertaService.cambiarActiva(id, request.activa());
    }
}
