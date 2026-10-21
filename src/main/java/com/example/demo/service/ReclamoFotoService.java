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
// archivo (FormatoImagen), y el nombre en Storage lo genera el servidor.
@Service
public class ReclamoFotoService {

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
        List<FormatoImagen> formatos = new ArrayList<>();
        for (MultipartFile archivo : recibidos) {
            if (archivo.getSize() > FormatoImagen.TAMANIO_MAXIMO_BYTES) {
                throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Cada imagen puede pesar hasta 5 MB");
            }
            byte[] bytes;
            try {
                bytes = archivo.getBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la imagen enviada", e);
            }
            contenidos.add(bytes);
            formatos.add(FormatoImagen.detectar(bytes));
        }

        List<String> rutasSubidas = new ArrayList<>();
        List<ReclamoFoto> guardadas = new ArrayList<>();
        try {
            short orden = (short) existentes;
            for (int i = 0; i < contenidos.size(); i++) {
                FormatoImagen formato = formatos.get(i);
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
}
