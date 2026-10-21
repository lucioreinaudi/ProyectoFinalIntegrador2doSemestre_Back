package com.example.demo.service;

import com.example.demo.dto.MetricasResponse;
import com.example.demo.dto.MetricasResponse.Cantidad;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Calcula las metricas del panel con pocas consultas agregadas (count / group by /
// avg en la BD), sin traer reclamos ni alertas a memoria.
@Service
@Transactional(readOnly = true)
public class MetricasService {

    // Orden y nombres de los estados en el grafico (incluye los que tengan 0).
    private static final Map<String, String> ESTADOS = new LinkedHashMap<>();

    static {
        ESTADOS.put(ReclamoEstadoService.RECIBIDO, "Recibido");
        ESTADOS.put(ReclamoEstadoService.EN_REVISION, "En revisión");
        ESTADOS.put(ReclamoEstadoService.EN_PROCESO, "En proceso");
        ESTADOS.put(ReclamoEstadoService.RESUELTO, "Resuelto");
        ESTADOS.put(ReclamoEstadoService.RECHAZADO, "Rechazado");
    }

    private final EntityManager em;

    public MetricasService(EntityManager em) {
        this.em = em;
    }

    public MetricasResponse calcular() {
        long totalReclamos = contar("select count(r) from Reclamo r");

        // Estado ACTUAL de cada reclamo = su registro mas nuevo (fecha, y el id desempata),
        // mismo criterio que el resto del back.
        List<Object[]> filasEstado = em.createQuery(
                "select e.estado, count(e) from EstadoReclamo e where not exists ("
                        + " select 1 from EstadoReclamo e2 where e2.reclamo = e.reclamo"
                        + " and (e2.fecha > e.fecha or (e2.fecha = e.fecha and e2.id > e.id)))"
                        + " group by e.estado", Object[].class).getResultList();
        Map<String, Long> porEstado = new HashMap<>();
        long conHistorial = 0;
        for (Object[] f : filasEstado) {
            porEstado.put((String) f[0], (Long) f[1]);
            conHistorial += (Long) f[1];
        }
        // Un reclamo viejo sin historial se cuenta como "recibido" (igual que en el resto del back).
        porEstado.merge(ReclamoEstadoService.RECIBIDO, totalReclamos - conHistorial, Long::sum);
        List<Cantidad> reclamosPorEstado = ESTADOS.entrySet().stream()
                .map(e -> new Cantidad(e.getKey(), e.getValue(), porEstado.getOrDefault(e.getKey(), 0L)))
                .toList();

        List<Cantidad> porCategoria = agrupar(
                "select c.nombre, count(r) from Reclamo r join r.categoria c group by c.nombre order by count(r) desc, c.nombre");
        List<Cantidad> porZona = agrupar(
                "select z.nombre, count(r) from Reclamo r join r.zona z group by z.nombre order by count(r) desc, z.nombre");

        long publicados = contar("select count(distinct ar.reclamo.id) from AlertaReclamo ar");
        long alertasActivas = contar("select count(a) from Alerta a where a.estado = 'activa'");
        long vecinos = contar("select count(u) from Usuario u where u.rol = com.example.demo.modelo.Rol.vecino");

        // Tiempo de resolucion: SQL nativo porque restar fechas y pasarlo a horas es propio de PostgreSQL.
        Object[] resolucion = (Object[]) em.createNativeQuery(
                "select avg(extract(epoch from (pr.primera - r.fecha_creacion))) / 3600.0, count(*)"
                        + " from \"Reclamo\" r"
                        + " join (select reclamo_id, min(fecha) as primera from \"EstadoReclamo\""
                        + "       where estado = 'resuelto' group by reclamo_id) pr"
                        + " on pr.reclamo_id = r.id_reclamo").getSingleResult();
        Double horasPromedio = resolucion[0] == null ? null : ((Number) resolucion[0]).doubleValue();
        long resueltosAlgunaVez = ((Number) resolucion[1]).longValue();

        return new MetricasResponse(totalReclamos, reclamosPorEstado, porCategoria, porZona,
                publicados, alertasActivas, vecinos, horasPromedio, resueltosAlgunaVez);
    }

    private long contar(String jpql) {
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    private List<Cantidad> agrupar(String jpql) {
        return em.createQuery(jpql, Object[].class).getResultList().stream()
                .map(f -> new Cantidad((String) f[0], (String) f[0], (Long) f[1]))
                .toList();
    }
}
