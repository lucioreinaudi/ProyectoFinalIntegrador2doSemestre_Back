package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

// Cliente minimo de Supabase Storage (API REST). Usa la service key, que vive solo
// en la variable de entorno SUPABASE_SERVICE_KEY: nunca llega al front ni al repo.
@Service
public class StorageService {

    private final String bucket;
    private final String baseUrl;
    private final String serviceKey;
    private final RestClient cliente;

    public StorageService(@Value("${supabase.url:}") String supabaseUrl,
                          @Value("${supabase.service-key:}") String serviceKey,
                          @Value("${supabase.bucket:reclamos}") String bucket) {
        this.baseUrl = supabaseUrl.endsWith("/") ? supabaseUrl.substring(0, supabaseUrl.length() - 1) : supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;
        this.cliente = RestClient.create();
    }

    // Sube el archivo y devuelve su URL publica.
    public String subir(String ruta, byte[] contenido, String contentType) {
        verificarConfiguracion();
        try {
            cliente.post()
                    .uri(baseUrl + "/storage/v1/object/" + bucket + "/" + ruta)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(contenido)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se pudo guardar la imagen en el almacenamiento", e);
        }
        return baseUrl + "/storage/v1/object/public/" + bucket + "/" + ruta;
    }

    // Best effort: se usa para deshacer una subida cuando algo falla despues.
    public void borrar(String ruta) {
        try {
            cliente.delete()
                    .uri(baseUrl + "/storage/v1/object/" + bucket + "/" + ruta)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ignorada) {
            // Si no se pudo borrar queda un archivo huerfano; no hay que tapar el error original.
        }
    }

    private void verificarConfiguracion() {
        if (baseUrl.isBlank() || serviceKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "El almacenamiento de imagenes no esta configurado");
        }
    }
}
