package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public record AlertaReclamoCrearRequest(
        @NotNull(message = "La alerta es obligatoria")
        Long alertaId,

        @NotNull(message = "El reclamo es obligatorio")
        Long reclamoId
) {
}
