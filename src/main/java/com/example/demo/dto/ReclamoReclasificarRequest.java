package com.example.demo.dto;

// Reclasificar un reclamo desde el panel: se puede mandar zonaId, categoriaId
// o los dos. El que venga en null no se cambia.
public record ReclamoReclasificarRequest(
        Long zonaId,
        Long categoriaId
) {
}
