package com.example.demo.service;

import com.example.demo.dto.TipoAlertaRequest;
import com.example.demo.modelo.TipoAlerta;
import com.example.demo.repositorio.TipoAlertaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// ABM de los tipos de alerta (Configuracion). No hay borrado fisico: las alertas
// apuntan al tipo (clave foranea), asi que se da de baja en vez de borrar.
@Service
public class TipoAlertaService {

    // Iconos que existen en el componente Icon del front. Si llega otro, se usa el generico.
    public static final Set<String> ICONOS = Set.of(
            "alert-triangle", "cloud-rain", "wind", "zap", "tree", "flame", "sun", "moon",
            "shield", "info", "megaphone", "map", "building", "heart", "phone");
    private static final String ICONO_GENERICO = "alert-triangle";

    private final TipoAlertaRepository tipoAlertaRepository;
    private final StorageService storageService;

    public TipoAlertaService(TipoAlertaRepository tipoAlertaRepository, StorageService storageService) {
        this.tipoAlertaRepository = tipoAlertaRepository;
        this.storageService = storageService;
    }

    public List<TipoAlerta> listar() {
        return tipoAlertaRepository.findAllByOrderByNombreAsc();
    }

    public TipoAlerta buscarOFallar(Long id) {
        return tipoAlertaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese tipo de alerta"));
    }

    @Transactional
    public TipoAlerta crear(TipoAlertaRequest datos) {
        String nombre = datos.nombre().trim();
        validarNombreLibre(nombre, null);
        TipoAlerta tipo = new TipoAlerta(generarClave(nombre), nombre, icono(datos.icono()),
                validarImagen(datos.imagenUrl(), null));
        return tipoAlertaRepository.save(tipo);
    }

    // Cambia nombre, icono e imagen. La clave no cambia (las alertas viejas la usan).
    @Transactional
    public TipoAlerta editar(Long id, TipoAlertaRequest datos) {
        TipoAlerta tipo = buscarOFallar(id);
        String nombre = datos.nombre().trim();
        validarNombreLibre(nombre, id);
        tipo.setNombre(nombre);
        tipo.setIcono(icono(datos.icono()));
        tipo.setImagenUrl(validarImagen(datos.imagenUrl(), tipo.getImagenUrl()));
        return tipoAlertaRepository.save(tipo);
    }

    @Transactional
    public TipoAlerta cambiarActiva(Long id, boolean activa) {
        TipoAlerta tipo = buscarOFallar(id);
        tipo.setActiva(activa);
        return tipoAlertaRepository.save(tipo);
    }

    // Lo usa AlertaService: el tipo tiene que existir y estar activo. Excepcion: al
    // editar una alerta que ya tenia ese tipo, aunque despues lo dieran de baja.
    public void validarParaAlerta(String clave, String claveActual) {
        TipoAlerta tipo = tipoAlertaRepository.findByClave(clave)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el tipo de alerta indicado"));
        if (!tipo.isActiva() && !clave.equals(claveActual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de alerta indicado esta dado de baja");
        }
    }

    // "Corte de calle" -> "corte-de-calle" (sin tildes, minusculas, hasta 30 caracteres).
    // Si ya existe, se le agrega un numero: "corte-de-calle-2".
    private String generarClave(String nombre) {
        String base = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) {
            base = "tipo";
        }
        base = base.length() > 26 ? base.substring(0, 26) : base;
        String clave = base;
        int n = 2;
        while (tipoAlertaRepository.existsByClave(clave)) {
            clave = base + "-" + n++;
        }
        return clave;
    }

    private void validarNombreLibre(String nombre, Long idActual) {
        tipoAlertaRepository.findByNombreIgnoreCase(nombre)
                .filter(otro -> !otro.getId().equals(idActual))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un tipo de alerta con ese nombre");
                });
    }

    private String icono(String icono) {
        return icono != null && ICONOS.contains(icono) ? icono : ICONO_GENERICO;
    }

    // Misma regla que la imagen de una alerta: solo de nuestro Storage. Si al
    // editar se deja la que ya tenia, se acepta igual.
    private String validarImagen(String imagenUrl, String actual) {
        if (imagenUrl == null || imagenUrl.isBlank()) {
            return null;
        }
        if (!imagenUrl.equals(actual) && !storageService.esUrlPropia(imagenUrl)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La imagen tiene que ser una subida desde el panel");
        }
        return imagenUrl;
    }
}
