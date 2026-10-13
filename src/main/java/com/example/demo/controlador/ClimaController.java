package com.example.demo.controlador;

import com.example.demo.dto.ClimaDTO;
import com.example.demo.service.ClimaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clima")
@CrossOrigin(origins = "*")
public class ClimaController {

    private final ClimaService climaService;

    public ClimaController(ClimaService climaService) {
        this.climaService = climaService;
    }

    @GetMapping("/actual")
    public ResponseEntity<ClimaDTO> getClimaActual() {
        ClimaDTO clima = climaService.obtenerClimaActual();
        return ResponseEntity.ok(clima);
    }
}