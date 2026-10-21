package com.example.demo.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.UUID;

// Sube a Storage una imagen suelta elegida desde el panel y devuelve su URL.
// La carpeta separa cada uso dentro del bucket:
//   alertas/       -> imagenes propias de una alerta
//   tipos-alerta/  -> imagenes de referencia de los tipos de alerta
// (las fotos de reclamos van en {idReclamo}/, ver ReclamoFotoService).
// Se sube antes de guardar el formulario; si el municipio cancela, el archivo
// queda sin usar en Storage (aceptado).
@Service
public class ImagenSubidaService {

    public static final String CARPETA_ALERTAS = "alertas";
    public static final String CARPETA_TIPOS_ALERTA = "tipos-alerta";

    private final StorageService storageService;

    public ImagenSubidaService(StorageService storageService) {
        this.storageService = storageService;
    }

    public String subir(MultipartFile archivo, String carpeta) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se envio ninguna imagen");
        }
        if (archivo.getSize() > FormatoImagen.TAMANIO_MAXIMO_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Cada imagen puede pesar hasta 5 MB");
        }
        byte[] bytes;
        try {
            bytes = archivo.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen enviada", e);
        }
        FormatoImagen formato = FormatoImagen.detectar(bytes);
        String ruta = carpeta + "/" + UUID.randomUUID() + "." + formato.extension();
        return storageService.subir(ruta, bytes, formato.contentType());
    }
}
