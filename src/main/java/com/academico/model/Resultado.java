package com.academico.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa la calificación obtenida por un alumno en una actividad específica.
 * Incluye lógica para calcular el impacto porcentual (aportación) en la unidad.
 */
public class Resultado {
    
    // === ATRIBUTOS DE PERSISTENCIA ===
    private int id;
    private int inscripcionId;
    private int actividadGrupoId;
    private BigDecimal calificacion; // Nulo significa que no ha sido evaluado
    private LocalDateTime modificadoEn;

    // === ATRIBUTOS DE VISUALIZACIÓN (JOINS) ===
    private String actividadNombre;
    private BigDecimal ponderacion;

    // === CONSTRUCTORES ===
    public Resultado() {}

    public Resultado(int id, int inscripcionId, int actividadGrupoId, BigDecimal calificacion, LocalDateTime modificadoEn) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.actividadGrupoId = actividadGrupoId;
        this.calificacion = calificacion;
        this.modificadoEn = modificadoEn;
    }

    // === LÓGICA DE NEGOCIO ===

    /**
     * Calcula cuántos puntos aporta esta actividad al promedio de la unidad (0-100).
     * Ejemplo: Calificación 100 * Ponderación 20% = 20 puntos de aportación.
     */
    public BigDecimal getAportacion() {
        if (calificacion == null || ponderacion == null) return BigDecimal.ZERO;
        return calificacion.multiply(ponderacion)
                           .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInscripcionId() { return inscripcionId; }
    public void setInscripcionId(int inscripcionId) { this.inscripcionId = inscripcionId; }

    public int getActividadGrupoId() { return actividadGrupoId; }
    public void setActividadGrupoId(int actividadGrupoId) { this.actividadGrupoId = actividadGrupoId; }

    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal calificacion) { this.calificacion = calificacion; }

    public LocalDateTime getModificadoEn() { return modificadoEn; }
    public void setModificadoEn(LocalDateTime modificadoEn) { this.modificadoEn = modificadoEn; }

    public String getActividadNombre() { return actividadNombre; }
    public void setActividadNombre(String actividadNombre) { this.actividadNombre = actividadNombre; }

    public BigDecimal getPonderacion() { return ponderacion; }
    public void setPonderacion(BigDecimal ponderacion) { this.ponderacion = ponderacion; }

    @Override
    public String toString() {
        return (actividadNombre != null ? actividadNombre : "Actividad") + ": " + (calificacion != null ? calificacion : "Pte.");
    }
}