package com.academico.calificaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Resultado {
    private int id;
    private int inscripcionId;
    private int actividadGrupoId;
    private BigDecimal calificacion; // nullable — null significa no presentó
    private LocalDateTime modificadoEn;
    // Desnormalización conveniente
    private String actividadNombre;
    private BigDecimal ponderacion;

    public Resultado() {}

    public Resultado(int id, int inscripcionId, int actividadGrupoId, BigDecimal calificacion, LocalDateTime modificadoEn) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.actividadGrupoId = actividadGrupoId;
        this.calificacion = calificacion;
        this.modificadoEn = modificadoEn;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getInscripcionId()                       { return inscripcionId; }
    public void setInscripcionId(int inscripcionId)     { this.inscripcionId = inscripcionId; }

    public int getActividadGrupoId()                            { return actividadGrupoId; }
    public void setActividadGrupoId(int actividadGrupoId)       { this.actividadGrupoId = actividadGrupoId; }

    public BigDecimal getCalificacion()                         { return calificacion; }
    public void setCalificacion(BigDecimal calificacion)        { this.calificacion = calificacion; }

    public LocalDateTime getModificadoEn()                          { return modificadoEn; }
    public void setModificadoEn(LocalDateTime modificadoEn)         { this.modificadoEn = modificadoEn; }

    public String getActividadNombre()                          { return actividadNombre; }
    public void setActividadNombre(String actividadNombre)      { this.actividadNombre = actividadNombre; }

    public BigDecimal getPonderacion()                      { return ponderacion; }
    public void setPonderacion(BigDecimal ponderacion)      { this.ponderacion = ponderacion; }

    // Calcula la aportación de esta actividad al promedio de la unidad
    public BigDecimal getAportacion() {
        if (calificacion == null || ponderacion == null) return BigDecimal.ZERO;
        return calificacion.multiply(ponderacion)
                           .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "Resultado{actividadGrupoId=" + actividadGrupoId
                + ", calificacion=" + calificacion + "}";
    }
}