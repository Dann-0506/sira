package com.academico.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Representa la relación entre un Alumno y un Grupo.
 * Almacena la fecha de registro y las calificaciones finales manuales (Overrides).
 */
public class Inscripcion {
    
    // === ATRIBUTOS DE PERSISTENCIA ===
    private int id;
    private int alumnoId;
    private int grupoId;
    private LocalDate fecha; 
    private BigDecimal calificacionFinalOverride; // Nulo si se usa el cálculo automático
    private String overrideJustificacion;

    // === ATRIBUTOS DE VISUALIZACIÓN (JOINS) ===
    private String alumnoNombre;
    private String alumnoMatricula;
    private String grupoClave; 

    // === CONSTRUCTORES ===
    public Inscripcion() {}

    public Inscripcion(int id, int alumnoId, int grupoId, LocalDate fecha) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.grupoId = grupoId;
        this.fecha = fecha;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getAlumnoId() { return alumnoId; }
    public void setAlumnoId(int alumnoId) { this.alumnoId = alumnoId; }

    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public BigDecimal getCalificacionFinalOverride() { return calificacionFinalOverride; }
    public void setCalificacionFinalOverride(BigDecimal calificacionFinalOverride) { this.calificacionFinalOverride = calificacionFinalOverride; }

    public String getOverrideJustificacion() { return overrideJustificacion; }
    public void setOverrideJustificacion(String overrideJustificacion) { this.overrideJustificacion = overrideJustificacion; }

    public String getAlumnoNombre() { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre) { this.alumnoNombre = alumnoNombre; }

    public String getAlumnoMatricula() { return alumnoMatricula; }
    public void setAlumnoMatricula(String alumnoMatricula) { this.alumnoMatricula = alumnoMatricula; }

    public String getGrupoClave() { return grupoClave; }
    public void setGrupoClave(String grupoClave) { this.grupoClave = grupoClave; }

    @Override
    public String toString() {
        return "Inscripción Alumno " + alumnoId + " en Grupo " + grupoId;
    }
}