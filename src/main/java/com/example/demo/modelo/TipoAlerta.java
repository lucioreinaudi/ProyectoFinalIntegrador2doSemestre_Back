package com.example.demo.modelo;

import jakarta.persistence.*;

// Tipo de alerta (tormenta, viento, ...), administrable desde Configuracion.
// Alerta.tipo guarda la "clave", que se genera al crear el tipo y no cambia nunca
// (asi las alertas viejas siguen apuntando bien aunque se renombre el tipo).
@Entity
@Table(name = "\"TipoAlerta\"")
public class TipoAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String clave;

    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    // Nombre de un icono del front (componente Icon). Default igual al de la BD.
    @Column(nullable = false, length = 30)
    private String icono = "alert-triangle";

    // Imagen de referencia que se muestra cuando una alerta de este tipo no tiene foto propia.
    @Column(name = "imagen_url", columnDefinition = "text")
    private String imagenUrl;

    // Baja logica: deja de ofrecerse al crear alertas; las alertas que ya lo usan no cambian.
    @Column(nullable = false)
    private boolean activa = true;

    public TipoAlerta() {
    }

    public TipoAlerta(String clave, String nombre, String icono, String imagenUrl) {
        this.clave = clave;
        this.nombre = nombre;
        this.icono = icono;
        this.imagenUrl = imagenUrl;
    }

    public Long getId() {
        return id;
    }

    public String getClave() {
        return clave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
