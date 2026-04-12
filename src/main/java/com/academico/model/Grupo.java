package com.academico.model;

import java.math.BigDecimal;

/**
 * Representa una instancia específica de una materia impartida por un docente.
 * Vincula el catálogo de materias con la asignación académica real.
 */
public class Grupo {
    
    // === ATRIBUTOS DE PERSISTENCIA ===
    private int id;
    private int materiaId;
    private int maestroId;
    private String clave;
    private String semestre;
    private boolean activo;
    private String estadoEvaluacion;
    private BigDecimal calificacionMinimaAprobatoria;
    private BigDecimal calificacionMaxima;

    // === ATRIBUTOS DE VISUALIZACIÓN (JOINS) ===
    private String materiaNombre;
    private String maestroNombre;
    
    // === ATRIBUTO CALCULADO ===
    private int totalAlumnos;

    // === CONSTRUCTORES ===
    public Grupo() {}

    public Grupo(int id, int materiaId, int maestroId, String clave, String semestre, boolean activo) {
        this.id = id;
        this.materiaId = materiaId;
        this.maestroId = maestroId;
        this.clave = clave;
        this.semestre = semestre;
        this.activo = activo;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMateriaId() { return materiaId; }
    public void setMateriaId(int materiaId) { this.materiaId = materiaId; }

    public int getMaestroId() { return maestroId; }
    public void setMaestroId(int maestroId) { this.maestroId = maestroId; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getMateriaNombre() { return materiaNombre; }
    public void setMateriaNombre(String materiaNombre) { this.materiaNombre = materiaNombre; }

    public String getMaestroNombre() { return maestroNombre; }
    public void setMaestroNombre(String maestroNombre) { this.maestroNombre = maestroNombre; }

    public String getEstadoEvaluacion() { return estadoEvaluacion; }
    public void setEstadoEvaluacion(String estadoEvaluacion) { this.estadoEvaluacion = estadoEvaluacion; }

    public int getTotalAlumnos() { return totalAlumnos; }
    public void setTotalAlumnos(int totalAlumnos) { this.totalAlumnos = totalAlumnos; }

    public BigDecimal getCalificacionMinimaAprobatoria() { return calificacionMinimaAprobatoria; }
    public void setCalificacionMinimaAprobatoria(BigDecimal calificacionMinimaAprobatoria) { this.calificacionMinimaAprobatoria = calificacionMinimaAprobatoria; }

    public BigDecimal getCalificacionMaxima() { return calificacionMaxima; }
    public void setCalificacionMaxima(BigDecimal calificacionMaxima) { this.calificacionMaxima = calificacionMaxima; }

    @Override
    public String toString() {
        return "[" + clave + "] " + (materiaNombre != null ? materiaNombre : "Materia " + materiaId) + " (" + semestre + ")";
    }

    public boolean isCerrado() { return "CERRADO".equals(estadoEvaluacion); }
}