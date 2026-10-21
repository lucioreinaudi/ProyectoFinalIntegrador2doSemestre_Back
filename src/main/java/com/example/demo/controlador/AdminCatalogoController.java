package com.example.demo.controlador;

import com.example.demo.modelo.Categoria;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.CategoriaRepository;
import com.example.demo.repositorio.ZonaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Zonas y categorias para el panel: TODAS, incluidas las dadas de baja (cada una
// trae "activa"), porque siguen en los filtros y en la pestana Configuracion (Q29).
// Solo municipio (/api/admin/**).
@RestController
@RequestMapping("/api/admin")
public class AdminCatalogoController {

    private final ZonaRepository zonaRepository;
    private final CategoriaRepository categoriaRepository;

    public AdminCatalogoController(ZonaRepository zonaRepository, CategoriaRepository categoriaRepository) {
        this.zonaRepository = zonaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping("/zonas")
    public List<Zona> zonas() {
        return zonaRepository.findAllByOrderByNombreAsc();
    }

    @GetMapping("/categorias")
    public List<Categoria> categorias() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }
}
