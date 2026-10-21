package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// Formatos de imagen aceptados (jpg, png, webp) y su deteccion por el contenido
// real del archivo (primeros bytes), no por el nombre ni por el Content-Type que
// mande el cliente. La usan las fotos de reclamos y las imagenes de alertas.
public record FormatoImagen(String extension, String contentType) {

    public static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;

    public static final FormatoImagen JPG = new FormatoImagen("jpg", "image/jpeg");
    public static final FormatoImagen PNG = new FormatoImagen("png", "image/png");
    public static final FormatoImagen WEBP = new FormatoImagen("webp", "image/webp");

    public static FormatoImagen detectar(byte[] b) {
        if (b.length >= 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return JPG;
        }
        if (b.length >= 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && b[4] == 0x0D && b[5] == 0x0A && b[6] == 0x1A && b[7] == 0x0A) {
            return PNG;
        }
        if (b.length >= 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return WEBP;
        }
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Formato no permitido: solo jpg, png o webp");
    }
}
