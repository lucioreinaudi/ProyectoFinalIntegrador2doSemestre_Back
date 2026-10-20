package com.example.demo.dto;

import com.example.demo.modelo.AlertaReclamo;
import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.Usuario;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Detalle completo de un reclamo para el panel (solo municipio): incluye los
// datos de contacto del vecino, que nunca se muestran en lo publico (Q10).
public record ReclamoDetalleResponse(
        Long id,
        String titulo,
        String descripcion,
        String fotoUrl,
        String ubicacion,
        Double lat,
        Double lng,
        OffsetDateTime fechaCreacion,
        Long zonaId,
        String zonaNombre,
        Long categoriaId,
        String categoriaNombre,
        String estadoActual,
        boolean publicado,
        Vecino vecino,
        List<EstadoReclamoResponse> historial,
        List<AlertaVinculada> alertas,
        List<String> fotos
) {
    public record Vecino(Long id, String nombre, String apellido, String email, String telefono) {
    }

    public record AlertaVinculada(Long id, String titulo, String estado, String nivelGravedad,
                                  OffsetDateTime fechaVinculacion) {
        public static AlertaVinculada de(AlertaReclamo ar) {
            return new AlertaVinculada(ar.getAlerta().getId(), ar.getAlerta().getTitulo(), ar.getAlerta().getEstado(),
                    ar.getAlerta().getNivelGravedad(), ar.getFechaVinculacion());
        }
    }

    // La ubicacion se guarda como texto "Lat: x, Lng: y" (asi la arma NuevoReporte).
    // Se convierte aca a numeros para que el mapa del panel no tenga que parsearla.
    private static final Pattern LAT_LNG = Pattern.compile(
            "Lat:\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*Lng:\\s*(-?\\d+(?:\\.\\d+)?)");

    public static ReclamoDetalleResponse de(Reclamo r, List<EstadoReclamoResponse> historial,
                                            List<AlertaVinculada> alertas, List<String> fotos) {
        Double lat = null;
        Double lng = null;
        if (r.getUbicacion() != null) {
            Matcher m = LAT_LNG.matcher(r.getUbicacion());
            if (m.find()) {
                lat = Double.valueOf(m.group(1));
                lng = Double.valueOf(m.group(2));
            }
        }

        Usuario u = r.getUsuario();
        Vecino vecino = new Vecino(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getTelefono());

        // El historial viene del mas viejo al mas nuevo: el ultimo es el actual.
        String estadoActual = historial.isEmpty() ? null : historial.get(historial.size() - 1).estado();

        return new ReclamoDetalleResponse(
                r.getId(), r.getTitulo(), r.getDescripcion(), r.getFotoUrl(), r.getUbicacion(), lat, lng,
                r.getFechaCreacion(),
                r.getZona().getId(), r.getZona().getNombre(),
                r.getCategoria().getId(), r.getCategoria().getNombre(),
                estadoActual, !alertas.isEmpty(), vecino, historial, alertas, fotos
        );
    }
}
