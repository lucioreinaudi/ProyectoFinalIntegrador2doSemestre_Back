package com.example.demo.service;

import com.example.demo.dto.AlertaCrearRequest;
import com.example.demo.dto.PublicarReclamoRequest;
import com.example.demo.modelo.Alerta;
import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.AlertaReclamoId;
import com.example.demo.modelo.Notificacion;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Usuario;
import com.example.demo.repositorio.AlertaReclamoRepository;
import com.example.demo.repositorio.EstadoReclamoRepository;
import com.example.demo.repositorio.NotificacionRepository;
import com.example.demo.repositorio.ReclamoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

// Publicar (aprobar) un reclamo = hacerlo publico a traves de una alerta (Q7, Q18).
// Todo en una transaccion: alerta (si es nueva) + vinculo + notificacion al vecino.
// Publicar NO cambia el estado del reclamo (son ejes separados, Q8), y el reclamo
// queda "publicado" porque existe el vinculo en AlertaReclamo.
@Service
public class PublicacionService {

    private final ReclamoRepository reclamoRepository;
    private final EstadoReclamoRepository estadoReclamoRepository;
    private final AlertaReclamoRepository alertaReclamoRepository;
    private final NotificacionRepository notificacionRepository;
    private final AlertaService alertaService;

    public PublicacionService(ReclamoRepository reclamoRepository, EstadoReclamoRepository estadoReclamoRepository,
                              AlertaReclamoRepository alertaReclamoRepository,
                              NotificacionRepository notificacionRepository, AlertaService alertaService) {
        this.reclamoRepository = reclamoRepository;
        this.estadoReclamoRepository = estadoReclamoRepository;
        this.alertaReclamoRepository = alertaReclamoRepository;
        this.notificacionRepository = notificacionRepository;
        this.alertaService = alertaService;
    }

    @Transactional
    public Alerta publicar(Long reclamoId, PublicarReclamoRequest request, Usuario autor) {
        boolean vincular = request.alertaId() != null;
        boolean crearNueva = request.alerta() != null;
        if (vincular == crearNueva) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica alertaId (vincular a una existente) o alerta (crear una nueva), pero no las dos");
        }

        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));

        // Un reclamo rechazado no se puede publicar (Q18).
        boolean rechazado = estadoReclamoRepository.findFirstByReclamoIdOrderByFechaDescIdDesc(reclamoId)
                .map(e -> ReclamoEstadoService.RECHAZADO.equals(e.getEstado()))
                .orElse(false);
        if (rechazado) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un reclamo rechazado no se puede publicar");
        }

        Alerta alerta;
        if (vincular) {
            alerta = alertaService.buscarOFallar(request.alertaId());
            // Para duplicados: solo se vincula a alertas que siguen activas.
            if (!"activa".equals(alerta.getEstado())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se puede vincular a una alerta activa");
            }
            if (alertaReclamoRepository.existsById(new AlertaReclamoId(alerta.getId(), reclamoId))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El reclamo ya esta vinculado a esa alerta");
            }
        } else {
            // La imagen ya viene elegida en imagenUrl (Q26); AlertaService la valida.
            alerta = alertaService.crear(request.alerta(), autor);
        }

        alertaReclamoRepository.save(new AlertaReclamo(alerta, reclamo));

        // Aviso al vecino (Q30), en la misma transaccion.
        notificacionRepository.save(new Notificacion(reclamo.getUsuario(), reclamo,
                "Tu reclamo '" + reclamo.getTitulo() + "' fue publicado como alerta"));

        return alerta;
    }
}
