package com.example.demo.controlador;

import com.example.demo.dto.MetricasResponse;
import com.example.demo.service.MetricasService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Pestana Metricas del panel. Solo municipio (/api/admin/**).
@RestController
@RequestMapping("/api/admin/metricas")
public class AdminMetricasController {

    private final MetricasService metricasService;

    public AdminMetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @GetMapping
    public MetricasResponse metricas() {
        return metricasService.calcular();
    }
}
