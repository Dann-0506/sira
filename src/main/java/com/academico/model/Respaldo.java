package com.academico.model;

import java.time.LocalDateTime;

public class Respaldo {
    private String nombreArchivo;
    private String rutaCompleta;
    private LocalDateTime fechaCreacion;
    private String tamanoMegabytes;

    public Respaldo(String nombreArchivo, String rutaCompleta, LocalDateTime fechaCreacion, String tamanoMegabytes) {
        this.nombreArchivo = nombreArchivo;
        this.rutaCompleta = rutaCompleta;
        this.fechaCreacion = fechaCreacion;
        this.tamanoMegabytes = tamanoMegabytes;
    }

    public String getNombreArchivo() { return nombreArchivo; }
    public String getRutaCompleta() { return rutaCompleta; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public String getTamanoMegabytes() { return tamanoMegabytes; }
}