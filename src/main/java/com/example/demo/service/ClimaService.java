package com.example.demo.service;

import com.example.demo.dto.ClimaDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ClimaService {

    // Coordenadas de Laboulaye por defecto
    @Value("${clima.latitude:-34.1263}")
    private String latitude;

    @Value("${clima.longitude:-63.3913}")
    private String longitude;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public ClimaDTO obtenerClimaActual() {
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
            e.printStackTrace();
        }

        return new ClimaDTO("--", "No disponible", 0, true, 0);
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