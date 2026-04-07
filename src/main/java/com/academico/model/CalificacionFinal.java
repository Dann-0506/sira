package com.academico.model;

import java.math.BigDecimal;
import java.util.List;

public class CalificacionFinal {
    private int inscripcionId;
    private int alumnoId;
    private String alumnoNombre;
    private String alumnoMatricula;
    private List<ResultadoUnidad> unidades;
    private BigDecimal calificacionCalculada;
    private BigDecimal bonusMateria;
    private BigDecimal calificacionConBonus;
    private BigDecimal calificacionFinal;   // override si existe, si no = conBonus
    private boolean esOverride;
    private String overrideJustificacion;

    public CalificacionFinal() {}

    public String getEstado() {
        if (calificacionFinal == null) return "PENDIENTE";
        return calificacionFinal.compareTo(BigDecimal.valueOf(70)) >= 0
                ? "APROBADO" : "REPROBADO";
    }

    public boolean isPendiente()    { return "PENDIENTE".equals(getEstado()); }
    public boolean isAprobado()     { return "APROBADO".equals(getEstado()); }
    public boolean isReprobado()    { return "REPROBADO".equals(getEstado()); }

    public int getInscripcionId()                           { return inscripcionId; }
    public void setInscripcionId(int inscripcionId)         { this.inscripcionId = inscripcionId; }

    public int getAlumnoId()                    { return alumnoId; }
    public void setAlumnoId(int alumnoId)       { this.alumnoId = alumnoId; }

    public String getAlumnoNombre()                         { return alumnoNombre; }
    public void setAlumnoNombre(String alumnoNombre)        { this.alumnoNombre = alumnoNombre; }

    public String getAlumnoMatricula()                          { return alumnoMatricula; }
    public void setAlumnoMatricula(String alumnoMatricula)      { this.alumnoMatricula = alumnoMatricula; }

    public List<ResultadoUnidad> getUnidades()              { return unidades; }
    public void setUnidades(List<ResultadoUnidad> unidades) { this.unidades = unidades; }

    public BigDecimal getCalificacionCalculada()                            { return calificacionCalculada; }
    public void setCalificacionCalculada(BigDecimal calificacionCalculada)  { this.calificacionCalculada = calificacionCalculada; }

    public BigDecimal getBonusMateria()                         { return bonusMateria; }
    public void setBonusMateria(BigDecimal bonusMateria)        { this.bonusMateria = bonusMateria; }

    public BigDecimal getCalificacionConBonus()                             { return calificacionConBonus; }
    public void setCalificacionConBonus(BigDecimal calificacionConBonus)    { this.calificacionConBonus = calificacionConBonus; }

    public BigDecimal getCalificacionFinal()                        { return calificacionFinal; }
    public void setCalificacionFinal(BigDecimal calificacionFinal)  { this.calificacionFinal = calificacionFinal; }

    public boolean isEsOverride()                   { return esOverride; }
    public void setEsOverride(boolean esOverride)   { this.esOverride = esOverride; }

    public String getOverrideJustificacion()                                { return overrideJustificacion; }
    public void setOverrideJustificacion(String overrideJustificacion)      { this.overrideJustificacion = overrideJustificacion; }
}