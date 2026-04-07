package com.academico.model;

import java.math.BigDecimal;
import java.util.List;

public class ResultadoUnidad {
    private int inscripcionId;
    private int unidadId;
    private int unidadNumero;
    private String unidadNombre;
    private List<Resultado> desglose;       // actividad por actividad
    private BigDecimal resultadoBase;       // promedio ponderado sin bonus
    private BigDecimal bonusPuntos;         // 0 si no hay bonus
    private BigDecimal resultadoFinal;      // base + bonus
    private int actividadesCalificadas;
    private int actividadesTotales;

    public ResultadoUnidad() {}

    // Estado derivado — no se almacena
    public String getEstado() {
        if (actividadesCalificadas < actividadesTotales) return "PENDIENTE";
        if (resultadoFinal == null) return "PENDIENTE";
        return resultadoFinal.compareTo(BigDecimal.valueOf(70)) >= 0
                ? "APROBADO" : "REPROBADO";
    }

    public boolean isPendiente()    { return "PENDIENTE".equals(getEstado()); }
    public boolean isAprobado()     { return "APROBADO".equals(getEstado()); }
    public boolean isReprobado()    { return "REPROBADO".equals(getEstado()); }

    public int getInscripcionId()                           { return inscripcionId; }
    public void setInscripcionId(int inscripcionId)         { this.inscripcionId = inscripcionId; }

    public int getUnidadId()                    { return unidadId; }
    public void setUnidadId(int unidadId)       { this.unidadId = unidadId; }

    public int getUnidadNumero()                        { return unidadNumero; }
    public void setUnidadNumero(int unidadNumero)       { this.unidadNumero = unidadNumero; }

    public String getUnidadNombre()                         { return unidadNombre; }
    public void setUnidadNombre(String unidadNombre)        { this.unidadNombre = unidadNombre; }

    public List<Resultado> getDesglose()                    { return desglose; }
    public void setDesglose(List<Resultado> desglose)       { this.desglose = desglose; }

    public BigDecimal getResultadoBase()                        { return resultadoBase; }
    public void setResultadoBase(BigDecimal resultadoBase)      { this.resultadoBase = resultadoBase; }

    public BigDecimal getBonusPuntos()                      { return bonusPuntos; }
    public void setBonusPuntos(BigDecimal bonusPuntos)      { this.bonusPuntos = bonusPuntos; }

    public BigDecimal getResultadoFinal()                       { return resultadoFinal; }
    public void setResultadoFinal(BigDecimal resultadoFinal)    { this.resultadoFinal = resultadoFinal; }

    public int getActividadesCalificadas()                              { return actividadesCalificadas; }
    public void setActividadesCalificadas(int actividadesCalificadas)   { this.actividadesCalificadas = actividadesCalificadas; }

    public int getActividadesTotales()                              { return actividadesTotales; }
    public void setActividadesTotales(int actividadesTotales)       { this.actividadesTotales = actividadesTotales; }
}