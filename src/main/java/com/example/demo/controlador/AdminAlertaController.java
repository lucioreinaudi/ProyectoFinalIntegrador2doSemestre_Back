package com.example.demo.controlador;

import com.example.demo.service.ImagenSubidaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

// Subida de imagenes desde el panel (el ABM de alertas esta en AlertaController y
// el de tipos en TipoAlertaController). Cada endpoint guarda en su propia carpeta
// del bucket. Todo /api/admin/** es solo para el municipio (SecurityConfig).
@RestController
@RequestMapping("/api/admin")
public class AdminAlertaController {

    private final ImagenSubidaService imagenSubidaService;

    public AdminAlertaController(ImagenSubidaService imagenSubidaService) {
        this.imagenSubidaService = imagenSubidaService;
    }

    // Imagen propia de una alerta (campo multipart "imagen", jpg/png/webp, hasta 5 MB).
    // Devuelve { "url": ... }, que despues se manda en imagenUrl al crear, editar o publicar.
    @PostMapping(value = "/alertas/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirImagenAlerta(@RequestParam("imagen") MultipartFile imagen) {
        String url = imagenSubidaService.subir(imagen, ImagenSubidaService.CARPETA_ALERTAS);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }

    // Imagen de referencia de un tipo de alerta. Mismo formato; va en tipos-alerta/.
    @PostMapping(value = "/tipos-alerta/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirImagenTipo(@RequestParam("imagen") MultipartFile imagen) {
        String url = imagenSubidaService.subir(imagen, ImagenSubidaService.CARPETA_TIPOS_ALERTA);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
    }
}
