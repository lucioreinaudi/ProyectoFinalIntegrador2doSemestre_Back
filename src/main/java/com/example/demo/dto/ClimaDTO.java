package com.example.demo.dto;

public class ClimaDTO {
    private String temperatura;
    private String condicion;
    private Integer icono;
    private Boolean esDeDia;
    private Integer humedad;

    public ClimaDTO(String temperatura, String condicion, Integer icono, Boolean esDeDia, Integer humedad) {
        this.temperatura = temperatura;
        this.condicion = condicion;
        this.icono = icono;
        this.esDeDia = esDeDia;
        this.humedad = humedad;
    }

    public String getTemperatura() { return temperatura; }
    public String getCondicion() { return condicion; }
    public Integer getIcono() { return icono; }
    public Boolean getEsDeDia() { return esDeDia; }
    public Integer getHumedad() { return humedad; }
}