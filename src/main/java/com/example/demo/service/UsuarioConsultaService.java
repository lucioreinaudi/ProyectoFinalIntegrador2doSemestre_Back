package com.example.demo.service;

import com.example.demo.dto.PaginaResponse;
import com.example.demo.dto.UsuarioAdminResponse;
import com.example.demo.modelo.Rol;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.ReclamoRepository;
import com.example.demo.repositorio.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Directorio de usuarios del panel (Q13): solo lectura, con filtros y la cantidad
// de reclamos de cada uno. Por pagina se hacen 2 consultas fijas (usuarios con su
// zona, y el conteo de reclamos de todos juntos), no una por usuario.
@Service
@Transactional(readOnly = true)
public class UsuarioConsultaService {

    private static final int TAMANIO_MAXIMO = 100;

    private final UsuarioRepository usuarioRepository;
    private final ReclamoRepository reclamoRepository;

    public UsuarioConsultaService(UsuarioRepository usuarioRepository, ReclamoRepository reclamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.reclamoRepository = reclamoRepository;
    }

    // texto: busca en nombre, apellido, DNI o email. Todos los filtros son opcionales.
    public PaginaResponse<UsuarioAdminResponse> listar(String texto, Long zonaId, String rol, int pagina, int tamanio) {
        if (pagina < 0 || tamanio < 1 || tamanio > TAMANIO_MAXIMO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "pagina tiene que ser 0 o mas, y tamanio entre 1 y " + TAMANIO_MAXIMO);
        }

        Specification<Usuario> spec = Specification.unrestricted();
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.get("apellido")), patron),
                    cb.like(cb.lower(root.get("documento")), patron),
                    cb.like(cb.lower(root.get("email")), patron)));
        }
        if (zonaId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("zona").get("id"), zonaId));
        }
        if (rol != null && !rol.isBlank()) {
            Rol rolBuscado = Arrays.stream(Rol.values()).filter(r -> r.name().equals(rol)).findFirst()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Rol invalido. Valores permitidos: " + Arrays.toString(Rol.values())));
            spec = spec.and((root, query, cb) -> cb.equal(root.get("rol"), rolBuscado));
        }

        // Los registrados mas recientemente primero (el id desempata).
        PageRequest pageRequest = PageRequest.of(pagina, tamanio,
                Sort.by(Sort.Direction.DESC, "fechaRegistro").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<Usuario> page = usuarioRepository.findAll(spec, pageRequest);

        List<Long> ids = page.getContent().stream().map(Usuario::getId).toList();
        Map<Long, Long> cantidades = new HashMap<>();
        if (!ids.isEmpty()) {
            for (Object[] fila : reclamoRepository.contarPorUsuario(ids)) {
                cantidades.put((Long) fila[0], (Long) fila[1]);
            }
        }

        List<UsuarioAdminResponse> contenido = page.getContent().stream()
                .map(u -> UsuarioAdminResponse.de(u, cantidades.getOrDefault(u.getId(), 0L)))
                .toList();
        return PaginaResponse.de(page, contenido);
    }
}
