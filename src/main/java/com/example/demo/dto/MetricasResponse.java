package com.example.demo.dto;

import java.util.List;

// Metricas del panel (Q15, seccion 7.3). Todo calculado en el back con consultas
// agregadas: el front no recibe tablas enteras, solo los numeros.
public record MetricasResponse(
        long totalReclamos,
        List<Cantidad> reclamosPorEstado,
        List<Cantidad> reclamosPorCategoria,
        List<Cantidad> reclamosPorZona,
        long reclamosPublicados,
        long alertasActivas,
        long vecinosRegistrados,
        // Promedio desde que se crea el reclamo hasta su PRIMER estado "resuelto".
        // null si todavia no se resolvio ninguno.
        Double horasPromedioResolucion,
        long reclamosResueltosAlgunaVez
) {
    // clave: valor tecnico (ej. "en revision"); nombre: como se muestra.
    public record Cantidad(String clave, String nombre, long cantidad) {
    }
}
