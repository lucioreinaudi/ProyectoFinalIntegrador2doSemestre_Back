package com.example.demo.controlador;

import com.example.demo.dto.CategoriaRequest;
import com.example.demo.modelo.Categoria;
import com.example.demo.repositorio.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Categoria obtener(@PathVariable Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe esa categoria"));
    }

    @PostMapping
    public ResponseEntity<Categoria> crear(@RequestBody CategoriaRequest request) {
        if (request.nombre() == null || request.nombre().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio");
        }
        if (categoriaRepository.findByNombre(request.nombre()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una categoria con ese nombre");
        }
        Categoria categoria = new Categoria(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaRepository.save(categoria));
    }
}
