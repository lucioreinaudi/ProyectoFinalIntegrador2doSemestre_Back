package com.example.demo.dto;

import jakarta.validation.Valid;

// Publicar un reclamo (Q18). Hay que mandar UNA de las dos opciones:
// - alertaId: vincularlo a una alerta activa que ya existe (duplicados).
// - alerta: crear una alerta nueva con estos datos (el front la precarga con
//   el titulo, descripcion y zona del reclamo, y el admin los ajusta).
// La imagen de la alerta nueva la elige el municipio (Q26) y viene en
// alerta.imagenUrl: una de las fotos del reclamo, una subida desde el panel
// (POST /api/admin/alertas/imagen) o ninguna.
public record PublicarReclamoRequest(
        Long alertaId,
        @Valid AlertaCrearRequest alerta
) {
}
