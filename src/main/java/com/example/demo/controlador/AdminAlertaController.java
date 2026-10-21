package com.example.demo.controlador;

import com.example.demo.service.AlertaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// Endpoints del panel sobre alertas que no son el ABM (ese esta en AlertaController).
// Todo /api/admin/** es solo para el municipio (regla en SecurityConfig).
@RestController
@RequestMapping("/api/admin/alertas")
public class AdminAlertaController {

    private final AlertaService alertaService;

    public AdminAlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    // Sube una imagen para una alerta (campo multipart "imagen", jpg/png/webp, hasta 5 MB)
    // y devuelve { "url": ... }. Despues esa URL se manda en imagenUrl al crear,
    // editar o publicar la alerta.
    @PostMapping(value = "/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirImagen(@RequestParam("imagen") MultipartFile imagen) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", alertaService.subirImagen(imagen)));
    }
}
