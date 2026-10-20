package com.example.demo.service;

import com.example.demo.modelo.Reclamo;
import com.example.demo.modelo.ReclamoFoto;
import com.example.demo.repositorio.ReclamoFotoRepository;
import com.example.demo.repositorio.ReclamoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Subida de fotos de un reclamo. El tipo se decide por el contenido real del
// archivo (primeros bytes), no por el nombre ni por el Content-Type que mande el
// cliente, y el nombre en Storage lo genera el servidor.
@Service
public class ReclamoFotoService {

    public static final long TAMANIO_MAXIMO_BYTES = 5L * 1024 * 1024;

    private record Formato(String extension, String contentType) {
    }

    private static final Formato JPG = new Formato("jpg", "image/jpeg");
    private static final Formato PNG = new Formato("png", "image/png");
    private static final Formato WEBP = new Formato("webp", "image/webp");

    private final ReclamoRepository reclamoRepository;
    private final ReclamoFotoRepository reclamoFotoRepository;
    private final StorageService storageService;
    private final int maximoPorReclamo;

    public ReclamoFotoService(ReclamoRepository reclamoRepository, ReclamoFotoRepository reclamoFotoRepository,
                              StorageService storageService,
                              @Value("${reclamos.fotos.maximo:5}") int maximoPorReclamo) {
        this.reclamoRepository = reclamoRepository;
        this.reclamoFotoRepository = reclamoFotoRepository;
        this.storageService = storageService;
        this.maximoPorReclamo = maximoPorReclamo;
    }

    // Solo el autor del reclamo puede agregarle fotos. Devuelve las URLs de todas sus fotos.
    public List<String> subir(Long reclamoId, String emailSolicitante, List<MultipartFile> archivos) {
        Reclamo reclamo = reclamoRepository.findById(reclamoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe ese reclamo"));
        if (!reclamo.getUsuario().getEmail().equals(emailSolicitante)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el autor del reclamo puede agregarle fotos");
        }

        List<MultipartFile> recibidos = archivos == null ? List.of()
                : archivos.stream().filter(a -> !a.isEmpty()).toList();
        if (recibidos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se envio ninguna imagen");
        }
        long existentes = reclamoFotoRepository.countByReclamoId(reclamoId);
        if (existentes + recibidos.size() > maximoPorReclamo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Un reclamo puede tener hasta " + maximoPorReclamo + " fotos (ya tiene " + existentes + ")");
        }

        // Se valida todo antes de subir algo, para no dejar subidas a medias por un archivo malo.
        List<byte[]> contenidos = new ArrayList<>();
        List<Formato> formatos = new ArrayList<>();
        for (MultipartFile archivo : recibidos) {
            if (archivo.getSize() > TAMANIO_MAXIMO_BYTES) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Cada imagen puede pesar hasta 5 MB");
            }
            byte[] bytes;
            try {
                bytes = archivo.getBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen enviada", e);
            }
            contenidos.add(bytes);
            formatos.add(detectarFormato(bytes));
        }

        List<String> rutasSubidas = new ArrayList<>();
        List<ReclamoFoto> guardadas = new ArrayList<>();
        try {
            short orden = (short) existentes;
            for (int i = 0; i < contenidos.size(); i++) {
                Formato formato = formatos.get(i);
                String ruta = reclamoId + "/" + UUID.randomUUID() + "." + formato.extension();
                String url = storageService.subir(ruta, contenidos.get(i), formato.contentType());
                rutasSubidas.add(ruta);
                guardadas.add(reclamoFotoRepository.save(new ReclamoFoto(reclamo, url, orden++)));
            }
            // foto_url (la portada) sigue siendo la primera foto: la usan el front actual y las alertas.
            if (reclamo.getFotoUrl() == null || reclamo.getFotoUrl().isBlank()) {
                reclamo.setFotoUrl(guardadas.get(0).getUrl());
                reclamoRepository.save(reclamo);
            }
        } catch (RuntimeException e) {
            // Fallo a mitad de camino: se deshace todo lo de este pedido (archivos y filas).
            rutasSubidas.forEach(storageService::borrar);
            reclamoFotoRepository.deleteAll(guardadas);
            throw e;
        }

        return reclamoFotoRepository.findByReclamoIdOrderByOrdenAscIdAsc(reclamoId).stream()
                .map(ReclamoFoto::getUrl).toList();
    }

    private Formato detectarFormato(byte[] b) {
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
