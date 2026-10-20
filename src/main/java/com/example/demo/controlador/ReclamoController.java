package com.example.demo.controlador;

import com.example.demo.dto.ReclamoCrearRequest;
import com.example.demo.dto.ReclamoResponse;
import com.example.demo.dto.ReclamoResumenResponse;
import com.example.demo.modelo.Categoria;
import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;
import com.example.demo.modelo.Zona;
import com.example.demo.repositorio.CategoriaRepository;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.ReclamoRepository;
import com.example.demo.repositorio.UsuarioRepository;
import com.example.demo.repositorio.ZonaRepository;
import com.example.demo.service.ReclamoConsultaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reclamos")
public class ReclamoController {

    private final ReclamoRepository reclamoRepository;
    private final ZonaRepository zonaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoReclamoRepository estadoReclamoRepository;
    private final ReclamoConsultaService reclamoConsultaService;

    public ReclamoController(ReclamoRepository reclamoRepository, ZonaRepository zonaRepository,
                              CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository,
                              EstadoReclamoRepository estadoReclamoRepository,
                              ReclamoConsultaService reclamoConsultaService) {
        this.reclamoRepository = reclamoRepository;
        this.zonaRepository = zonaRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.reclamoConsultaService = reclamoConsultaService;
    }

    @GetMapping
    public List<ReclamoResponse> listar() {
        return reclamoRepository.findAll().stream().map(ReclamoResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ReclamoResponse obtener(@PathVariable Long id) {
        return ReclamoResponse.de(buscarOFallar(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<ReclamoResponse> porUsuario(@PathVariable Long usuarioId) {
        return reclamoRepository.findByUsuarioId(usuarioId).stream().map(ReclamoResponse::de).toList();
    }

    // Los reclamos del propio vecino, sacando el usuario del token (no de un id en la
    // URL): asi nadie puede ver reclamos ajenos cambiando un numero en el pedido.
    // Incluye estado actual y "publicado", asi Mis Reportes no tiene que pedir el
    // estado de cada reclamo por separado. Del mas nuevo al mas viejo.
    @GetMapping("/me")
    public List<ReclamoResumenResponse> misReclamos(Authentication authentication) {
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
        return reclamoConsultaService.listarDelVecino(usuario.getId());
    }

    @GetMapping("/zona/{zonaId}")
    public List<ReclamoResponse> porZona(@PathVariable Long zonaId) {
        return reclamoRepository.findByZonaId(zonaId).stream().map(ReclamoResponse::de).toList();
    }

    @GetMapping("/categoria/{categoriaId}")
    public List<ReclamoResponse> porCategoria(@PathVariable Long categoriaId) {
        return reclamoRepository.findByCategoriaId(categoriaId).stream().map(ReclamoResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<ReclamoResponse> crear(@Valid @RequestBody ReclamoCrearRequest request,
                                                   Authentication authentication) {
        // El autor es quien esta logueado (el filtro JWT deja el email como "name"
        // de la autenticacion), nunca un id que mande el cliente en el body.
        Usuario usuario = usuarioRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));

        // El municipio no reporta problemas, solo los gestiona.
        if (usuario.getRol() == Rol.municipio) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El municipio no puede crear reclamos");
        }

        Zona zona = zonaRepository.findById(request.zonaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la zona indicada"));
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe la categoria indicada"));

        Reclamo reclamo = new Reclamo();
        reclamo.setTitulo(request.titulo());
        reclamo.setDescripcion(request.descripcion());
        reclamo.setFotoUrl(request.fotoUrl());
        reclamo.setUbicacion(request.ubicacion());
        reclamo.setZona(zona);
        reclamo.setCategoria(categoria);
        reclamo.setUsuario(usuario);

        Reclamo guardado = reclamoRepository.save(reclamo);

        // Todo reclamo nace con su primer estado en el historial, para que
        // "estado actual" siempre tenga algo que devolver.
        estadoReclamoRepository.save(new EstadoReclamo(guardado, "recibido", null));

        return ResponseEntity.status(HttpStatus.CREATED).body(ReclamoResponse.de(guardado));
    }

    private Reclamo buscarOFallar(Long id) {
        return reclamoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));
    }
}
