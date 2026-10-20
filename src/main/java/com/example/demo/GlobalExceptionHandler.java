package com.example.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

// Convierte los errores de @Valid en una respuesta 400 con el mismo formato
// (ProblemDetail, campo "detail") que ya usan los ResponseStatusException de
// los controladores, para que el front no tenga que manejar dos formatos
// de error distintos.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail manejarValidacion(MethodArgumentNotValidException ex) {
        String detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(". "));

        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setDetail(detalle);
        return problema;
    }

    // Sin esto, el ResponseStatusException se resuelve con sendError y el despacho a /error
    // lo bloquea Spring Security: el cliente recibia un 403 vacio en lugar del codigo real.
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail manejarEstado(ResponseStatusException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(ex.getStatusCode());
        problema.setDetail(ex.getReason());
        return problema;
    }

    // Pedido multipart sin el campo esperado (ej. falta "fotos"): Spring lo corta antes del
    // controlador y, sin handler, terminaba igual en el 403 vacio.
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ProblemDetail manejarParteFaltante(MissingServletRequestPartException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problema.setDetail("Falta el campo '" + ex.getRequestPartName() + "'");
        return problema;
    }

    // Lo corta Spring antes de llegar al controlador, por eso necesita su propio manejo.
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail manejarArchivoGrande(MaxUploadSizeExceededException ex) {
        ProblemDetail problema = ProblemDetail.forStatus(HttpStatus.PAYLOAD_TOO_LARGE);
        problema.setDetail("Cada imagen puede pesar hasta 5 MB");
        return problema;
    }
}
