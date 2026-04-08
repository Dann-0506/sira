package com.academico.inscripciones;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Inscripcion {
    private int id;
    private int alumnoId;
    private int grupoId;
    private LocalDate fecha;   // fecha de inscripción
    private BigDecimal calificacionFinalOverride; // null si no hay override
    private String overrideJustificacion; // motivo del override
    // Desnormalizaciones convenientes para vistas
    private String alumnoNombre;
    private String alumnoMatricula;
    private String grupoClave; 


    public Inscripcion() {}

    public Inscripcion(int id, int alumnoId, int grupoId, LocalDate fecha) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.grupoId = grupoId;
        this.fecha = fecha;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public int getAlumnoId()                    { return alumnoId; }
    public void setAlumnoId(int alumnoId)       { this.alumnoId = alumnoId;}

    public int getGrupoId()                    { return grupoId; }
    public void setGrupoId(int grupoId)       { this.grupoId = grupoId;}

    public LocalDate getFecha()                   { return fecha; }
    public void setFecha(LocalDate fecha)         { this.fecha = fecha; }

    public BigDecimal getCalificacionFinalOverride() { return calificacionFinalOverride; }
    public void setCalificacionFinalOverride(BigDecimal calificacionFinalOverride) { this.calificacionFinalOverride = calificacionFinalOverride; }

    public String getOverrideJustificacion() { return overrideJustificacion; }
    public void setOverrideJustificacion(String overrideJustificacion) { this.overrideJustificacion = overrideJustificacion; }


    public String getAlumnoNombre()                         { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre)        { this.alumnoNombre = alumnoNombre; }

    public String getAlumnoMatricula()                          { return alumnoMatricula; }
    public void setAlumnoMatricula(String alumnoMatricula)      { this.alumnoMatricula = alumnoMatricula; }

    public String getGrupoClave()                          { return grupoClave; }
    public void setGrupoClave(String grupoClave)      { this.grupoClave = grupoClave; }

    @Override
    public String toString() {
        return "Inscripcion{" +
                "id=" + id +
                ", alumnoId=" + alumnoId +
                ", grupoId=" + grupoId +
                ", fecha=" + fecha +
                ", calificacionFinalOverride=" + calificacionFinalOverride +
                ", overrideJustificacion='" + overrideJustificacion + '\'' +
                ", alumnoNombre='" + alumnoNombre + '\'' +
                ", alumnoMatricula='" + alumnoMatricula + '\'' +
                ", grupoClave='" + grupoClave + '\'' +
                '}';
    }
}
