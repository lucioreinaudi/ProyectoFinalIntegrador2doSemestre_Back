package com.example.demo.dto;

import org.springframework.data.domain.Page;

import java.util.List;

// Respuesta paginada con un formato propio y estable (devolver el Page de Spring
// directo expone detalles internos que pueden cambiar entre versiones).
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamanio,
        long totalElementos,
        int totalPaginas
) {
    public static <T> PaginaResponse<T> de(Page<?> page, List<T> contenido) {
        return new PaginaResponse<>(contenido, page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
