package com.example.demo.service;

import com.example.demo.modelo.EstadoReclamo;
import com.example.demo.modelo.Notificacion;
import com.example.demo.modelo.Reclamo;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.NotificacionRepository;
import com.example.demo.repositorio.ReclamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

// Reglas para cambiar el estado de un reclamo (tabla de transiciones de la
// especificacion del panel, seccion 5.1). Esta en un service, y no en el
// controlador, por dos motivos:
// - El estado nuevo y la notificacion al vecino se tienen que guardar juntos
//   (@Transactional): si falla uno, no queda ninguno.
// - Lo va a reutilizar "publicar reclamo", que tambien notifica.
@Service
public class ReclamoEstadoService {

    public static final String RECIBIDO = "recibido";
    public static final String EN_REVISION = "en revision";
    public static final String EN_PROCESO = "en proceso";
    public static final String RESUELTO = "resuelto";
    public static final String RECHAZADO = "rechazado";

    // Desde cada estado, a cuales se puede pasar. Hacia adelante es libre;
    // resuelto y rechazado son finales, pero se pueden reabrir a "en revision".
    // Las claves son tambien la lista de estados validos (mismos valores que
    // el CHECK de la tabla EstadoReclamo en la BD).
    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            RECIBIDO, Set.of(EN_REVISION, EN_PROCESO, RESUELTO, RECHAZADO),
            EN_REVISION, Set.of(EN_PROCESO, RESUELTO, RECHAZADO),
            EN_PROCESO, Set.of(RESUELTO, RECHAZADO),
            RESUELTO, Set.of(EN_REVISION),
            RECHAZADO, Set.of(EN_REVISION)
    );

    private static final Set<String> FINALES = Set.of(RESUELTO, RECHAZADO);

    // Todos los estados validos (lo usa tambien el filtro por estado del panel).
    public static final Set<String> ESTADOS = TRANSICIONES.keySet();

    // Como se muestra cada estado en el mensaje de la notificacion (en la BD
    // van en minuscula y sin tilde).
    private static final Map<String, String> NOMBRE_VISIBLE = Map.of(
            RECIBIDO, "Recibido",
            EN_REVISION, "En revisión",
            EN_PROCESO, "En proceso",
            RESUELTO, "Resuelto",
            RECHAZADO, "Rechazado"
    );

    private final ReclamoRepository reclamoRepository;
    private final EstadoReclamoRepository estadoReclamoRepository;
    private final NotificacionRepository notificacionRepository;

    public ReclamoEstadoService(ReclamoRepository reclamoRepository, EstadoReclamoRepository estadoReclamoRepository,
                                NotificacionRepository notificacionRepository) {
        this.reclamoRepository = reclamoRepository;
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.notificacionRepository = notificacionRepository;
    }

    @Transactional
    public EstadoReclamo cambiarEstado(Long reclamoId, String estadoNuevo, String comentario) {
        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el reclamo indicado"));

        if (!TRANSICIONES.containsKey(estadoNuevo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado invalido. Valores permitidos: " + ESTADOS);
        }

        // Un comentario con solo espacios cuenta como "sin comentario".
        String comentarioLimpio = (comentario == null || comentario.isBlank()) ? null : comentario.trim();

        // Todo reclamo nace con "recibido" (ReclamoController.crear). Si por algun
        // dato viejo no tuviera historial, se lo trata como recibido.
        String estadoActual = estadoReclamoRepository.findFirstByReclamoIdOrderByFechaDescIdDesc(reclamoId)
                .map(EstadoReclamo::getEstado)
                .orElse(RECIBIDO);

        validarCambio(estadoActual, estadoNuevo, comentarioLimpio);

        EstadoReclamo guardado = estadoReclamoRepository.save(new EstadoReclamo(reclamo, estadoNuevo, comentarioLimpio));

        // Notificacion automatica al duenio del reclamo (Q11), en la misma transaccion.
        String mensaje = armarMensaje(reclamo, estadoActual, estadoNuevo, comentarioLimpio);
        notificacionRepository.save(new Notificacion(reclamo.getUsuario(), reclamo, mensaje));

        return guardado;
    }

    private void validarCambio(String actual, String nuevo, String comentario) {
        // Mismo estado: sirve para dejarle una novedad al vecino sin cambiar el
        // estado, asi que el comentario es lo unico que aporta y es obligatorio.
        // En los estados finales no se permite (para eso esta la reapertura).
        if (actual.equals(nuevo)) {
            if (FINALES.contains(actual)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El reclamo esta " + actual + ". Solo se puede reabrir pasando a '" + EN_REVISION + "'");
            }
            if (comentario == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Para dejar una novedad sin cambiar el estado, el comentario es obligatorio");
            }
            return;
        }

        if (!TRANSICIONES.get(actual).contains(nuevo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede pasar de '" + actual + "' a '" + nuevo + "'. Desde '" + actual
                            + "' se permite: " + TRANSICIONES.get(actual));
        }

        // Comentario obligatorio al rechazar (es el motivo que ve el vecino) y al reabrir.
        if (comentario == null && RECHAZADO.equals(nuevo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para rechazar un reclamo hay que indicar el motivo");
        }
        if (comentario == null && FINALES.contains(actual)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Para reabrir un reclamo hay que indicar el motivo");
        }
    }

    private String armarMensaje(Reclamo reclamo, String actual, String nuevo, String comentario) {
        String base = actual.equals(nuevo)
                ? "Hay novedades en tu reclamo '" + reclamo.getTitulo() + "'"
                : "Tu reclamo '" + reclamo.getTitulo() + "' pasó a " + NOMBRE_VISIBLE.get(nuevo);
        return comentario == null ? base : base + ": " + comentario;
    }
}
