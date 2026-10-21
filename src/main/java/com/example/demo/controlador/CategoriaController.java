package com.example.demo.controlador;

import com.example.demo.dto.ActivaRequest;
import com.example.demo.dto.CategoriaRequest;
import com.example.demo.modelo.Categoria;
import com.example.demo.repositorio.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// Mismo criterio que ZonaController: GET publicos con solo las activas (Q16), el
// panel usa GET /api/admin/categorias (todas, Q29), y crear/editar/dar de baja es
// solo para el municipio. Sin borrado fisico: los reclamos apuntan a la categoria.
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findByActivaTrueOrderByNombreAsc();
    }

    @GetMapping("/{id}")
    public Categoria obtener(@PathVariable Long id) {
        return buscarOFallar(id);
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@Valid @RequestBody CategoriaRequest request) {
        String nombre = request.nombre().trim();
        validarNombreLibre(nombre, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepository.save(new Categoria(nombre)));
    }

    @PutMapping("/{id}")
    public Categoria editar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        Categoria categoria = buscarOFallar(id);
        String nombre = request.nombre().trim();
        validarNombreLibre(nombre, id);
        categoria.setNombre(nombre);
        return categoriaRepository.save(categoria);
    }

    // Baja logica (activa=false) o reactivacion (activa=true).
    @PatchMapping("/{id}/activa")
    public Categoria cambiarActiva(@PathVariable Long id, @Valid @RequestBody ActivaRequest request) {
        Categoria categoria = buscarOFallar(id);
        categoria.setActiva(request.activa());
        return categoriaRepository.save(categoria);
    }

    private Categoria buscarOFallar(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa categoria"));
    }

    private void validarNombreLibre(String nombre, Long idActual) {
        categoriaRepository.findByNombreIgnoreCase(nombre)
                .filter(otra -> !otra.getId().equals(idActual))
                .ifPresent(otra -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoria con ese nombre");
                });
    }
}
