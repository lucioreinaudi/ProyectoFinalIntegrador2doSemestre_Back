package com.example.demo.dto;

import jakarta.validation.Valid;

// Publicar un reclamo (Q18). Hay que mandar UNA de las dos opciones:
// - alertaId: vincularlo a una alerta activa que ya existe (duplicados).
// - alerta: crear una alerta nueva con estos datos (el front la precarga con
//   el titulo, descripcion y zona del reclamo, y el admin los ajusta).
// incluirFoto: si es true y el reclamo tiene foto, la alerta nueva la usa
// (salvo que ya venga una imagenUrl propia). No aplica al vincular (Q26).
public record PublicarReclamoRequest(
        Long alertaId,
        @Valid AlertaCrearRequest alerta,
        boolean incluirFoto
) {
}
