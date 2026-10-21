package com.example.demo.controlador;

import com.example.demo.dto.ActivaRequest;
import com.example.demo.dto.ZonaRequest;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.ZonaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Zona no devuelve passwordHash ni nada sensible, asi que se puede usar la entidad
// directa como respuesta, sin necesidad de un DTO de salida.
// Los GET son publicos (formularios del vecino y filtro del Home) y devuelven solo
// las activas (Q16). El panel usa GET /api/admin/zonas, que devuelve todas (Q29).
// Crear, editar y dar de baja: solo municipio (SecurityConfig). No hay borrado
// fisico: reclamos, alertas y usuarios apuntan a la zona.
@RestController
@RequestMapping("/api/zonas")
public class ZonaController {

    private final ZonaRepository zonaRepository;

    public ZonaController(ZonaRepository zonaRepository) {
        this.zonaRepository = zonaRepository;
    }

    @GetMapping
    public List<Zona> listar() {
        return zonaRepository.findByActivaTrueOrderByNombreAsc();
    }

    @GetMapping("/{id}")
    public Zona obtener(@PathVariable Long id) {
        return buscarOFallar(id);
    }

    @PostMapping
    public ResponseEntity<Zona> crear(@Valid @RequestBody ZonaRequest request) {
        String nombre = request.nombre().trim();
        validarNombreLibre(nombre, null);
        Zona zona = new Zona(nombre, limpiar(request.descripcion()));
        return ResponseEntity.status(HttpStatus.CREATED).body(zonaRepository.save(zona));
    }

    // Renombrar y/o cambiar la descripcion.
    @PutMapping("/{id}")
    public Zona editar(@PathVariable Long id, @Valid @RequestBody ZonaRequest request) {
        Zona zona = buscarOFallar(id);
        String nombre = request.nombre().trim();
        validarNombreLibre(nombre, id);
        zona.setNombre(nombre);
        zona.setDescripcion(limpiar(request.descripcion()));
        return zonaRepository.save(zona);
    }

    // Baja logica (activa=false) o reactivacion (activa=true). Al darla de baja deja
    // de aparecer en "Reportar problema" y en los formularios del panel; los
    // registros que ya la usan no cambian (Q16, Q29).
    @PatchMapping("/{id}/activa")
    public Zona cambiarActiva(@PathVariable Long id, @Valid @RequestBody ActivaRequest request) {
        Zona zona = buscarOFallar(id);
        zona.setActiva(request.activa());
        return zonaRepository.save(zona);
    }

    private Zona buscarOFallar(Long id) {
        return zonaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa zona"));
    }

    // idActual: al editar, la propia zona no cuenta como repetida.
    private void validarNombreLibre(String nombre, Long idActual) {
        zonaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una zona con ese nombre");
                });
    }

    private String limpiar(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
