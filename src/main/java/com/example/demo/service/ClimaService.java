package com.example.demo.service;

import com.example.demo.dto.ClimaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ClimaService {

    // Coordenadas de Laboulaye por defecto
    @Value("${clima.latitude:-34.1263}")
    private String latitude;

    @Value("${clima.longitude:-63.3913}")
    private String longitude;

    // Los proveedores actualizan cada ~15 min y Open-Meteo limita por IP (en hosting compartido se agota), asi que se cachea.
    private static final long CACHE_MS = 15 * 60 * 1000L;
    private static final long REINTENTO_MS = 60 * 1000L;

    private final RestTemplate restTemplate = new RestTemplate();
    private ClimaDTO ultimoClima;
    private long proximaConsulta = 0;

    public synchronized ClimaDTO obtenerClimaActual() {
        long ahora = System.currentTimeMillis();
        if (ultimoClima != null && ahora < proximaConsulta) {
            return ultimoClima;
        }

        ClimaDTO clima = consultarOpenMeteo();
        if (clima == null) {
            clima = consultarMetNoruega();
        }

        if (clima != null) {
            ultimoClima = clima;
            proximaConsulta = ahora + CACHE_MS;
            return clima;
        }

        proximaConsulta = ahora + REINTENTO_MS;
        if (ultimoClima != null) {
            return ultimoClima;
        }
        return new ClimaDTO("--", "No disponible", 0, true, 0);
    }

    @SuppressWarnings("unchecked")
    private ClimaDTO consultarOpenMeteo() {
        String url = String.format(
                "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m,relative_humidity_2m,is_day,weather_code&timezone=America/Argentina/Buenos_Aires",
                latitude, longitude
        );

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("current")) {
                Map<String, Object> current = (Map<String, Object>) response.get("current");

                Object tempObj = current.get("temperature_2m");
                String temp = (tempObj != null) ? Math.round(Double.parseDouble(tempObj.toString())) + "°C" : "--°C";

                Integer humedad = (current.get("relative_humidity_2m") != null)
                        ? ((Number) current.get("relative_humidity_2m")).intValue() : 0;

                Integer isDayInt = (current.get("is_day") != null)
                        ? ((Number) current.get("is_day")).intValue() : 1;
                Boolean esDeDia = isDayInt == 1;

                Integer weatherCode = (current.get("weather_code") != null)
                        ? ((Number) current.get("weather_code")).intValue() : 0;

                String condicion = obtenerDescripcionWMO(weatherCode);

                return new ClimaDTO(temp, condicion, weatherCode, esDeDia, humedad);
            }
        } catch (Exception e) {
            System.err.println("Open-Meteo no disponible: " + e.getMessage());
        }
        return null;
    }

    // Respaldo gratuito y sin clave (exige un User-Agent que identifique la app).
    @SuppressWarnings("unchecked")
    private ClimaDTO consultarMetNoruega() {
        String url = String.format(
                "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%s&lon=%s",
                latitude, longitude
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "ConectarLaboulaye/1.0 (proyecto final integrador)");
            Map<String, Object> response = restTemplate
                    .exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();

            Map<String, Object> properties = (Map<String, Object>) response.get("properties");
            List<Map<String, Object>> serie = (List<Map<String, Object>>) properties.get("timeseries");
            Map<String, Object> data = (Map<String, Object>) serie.get(0).get("data");
            Map<String, Object> detalles = (Map<String, Object>) ((Map<String, Object>) data.get("instant")).get("details");

            String temp = Math.round(((Number) detalles.get("air_temperature")).doubleValue()) + "°C";
            Integer humedad = (detalles.get("relative_humidity") != null)
                    ? ((Number) detalles.get("relative_humidity")).intValue() : 0;

            String simbolo = "clearsky_day";
            Map<String, Object> proxima = (Map<String, Object>) data.get("next_1_hours");
            if (proxima != null) {
                simbolo = (String) ((Map<String, Object>) proxima.get("summary")).get("symbol_code");
            }

            Boolean esDeDia = !simbolo.endsWith("_night");
            int weatherCode = codigoWMODesdeSimbolo(simbolo);

            return new ClimaDTO(temp, obtenerDescripcionWMO(weatherCode), weatherCode, esDeDia, humedad);
        } catch (Exception e) {
            System.err.println("MET Noruega no disponible: " + e.getMessage());
        }
        return null;
    }

    // Traduce el symbol_code de MET Noruega (ej. "lightrain_day") a un codigo WMO.
    private int codigoWMODesdeSimbolo(String simbolo) {
        String s = simbolo.replaceAll("_(day|night|polartwilight)$", "");
        if (s.contains("thunder")) return 95;
        if (s.contains("snow") || s.contains("sleet")) return 71;
        if (s.contains("showers")) return 80;
        if (s.equals("heavyrain")) return 65;
        if (s.contains("rain")) return 61;
        if (s.equals("fog")) return 45;
        if (s.equals("cloudy")) return 3;
        if (s.equals("partlycloudy") || s.equals("fair")) return 2;
        return 0;
    }

    private String obtenerDescripcionWMO(int code) {
        return switch (code) {
            case 0 -> "Despejado";
            case 1, 2, 3 -> "Parcialmente nublado";
            case 45, 48 -> "Niebla";
            case 51, 53, 55 -> "Llovizna";
            case 61, 63, 65 -> "Lluvia";
            case 71, 73, 75 -> "Nieve";
            case 80, 81, 82 -> "Chubascos";
            case 95, 96, 99 -> "Tormenta eléctrica";
            default -> "Clima variable";
        };
    }
}
