package com.academico.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO que representa el desempeño consolidado de un alumno en una unidad específica.
 * Realiza cálculos derivados sobre el estado de aprobación.
 */
public class ResultadoUnidad {
    
    // === IDENTIFICADORES ===
    private int inscripcionId;
    private int unidadId;
    private int unidadNumero;
    private String unidadNombre;

    // === DATOS DE CALIFICACIÓN ===
    private List<Resultado> desglose;       // Detalle actividad por actividad
    private BigDecimal resultadoBase;       // Promedio ponderado sin puntos extra
    private BigDecimal bonusPuntos;         // Puntos extra (0 si no hay)
    private BigDecimal resultadoFinal;      // Suma de base + bonus (Max 100.00)

    // === METADATOS DE AVANCE ===
    private int actividadesCalificadas;
    private int actividadesTotales;

    public ResultadoUnidad() {}

    // === LÓGICA DE NEGOCIO (ESTADO DERIVADO) ===

    /**
     * Determina el estado académico de la unidad basándose en las actividades calificadas.
     */
    public String getEstado() {
        if (actividadesCalificadas < actividadesTotales) return "PENDIENTE";
        if (resultadoFinal == null) return "PENDIENTE";
        
        return resultadoFinal.compareTo(BigDecimal.valueOf(70)) >= 0 
                ? "APROBADO" : "REPROBADO";
    }

    public boolean isPendiente() { return "PENDIENTE".equals(getEstado()); }
    public boolean isAprobado()  { return "APROBADO".equals(getEstado()); }
    public boolean isReprobado() { return "REPROBADO".equals(getEstado()); }

    // === GETTERS Y SETTERS ===
    public int getInscripcionId() { return inscripcionId; }
    public void setInscripcionId(int inscripcionId) { this.inscripcionId = inscripcionId; }

    public int getUnidadId() { return unidadId; }
    public void setUnidadId(int unidadId) { this.unidadId = unidadId; }

    public int getUnidadNumero() { return unidadNumero; }
    public void setUnidadNumero(int unidadNumero) { this.unidadNumero = unidadNumero; }

    public String getUnidadNombre() { return unidadNombre; }
    public void setUnidadNombre(String unidadNombre) { this.unidadNombre = unidadNombre; }

    public List<Resultado> getDesglose() { return desglose; }
    public void setDesglose(List<Resultado> desglose) { this.desglose = desglose; }

    public BigDecimal getResultadoBase() { return resultadoBase; }
    public void setResultadoBase(BigDecimal resultadoBase) { this.resultadoBase = resultadoBase; }

    public BigDecimal getBonusPuntos() { return bonusPuntos; }
    public void setBonusPuntos(BigDecimal bonusPuntos) { this.bonusPuntos = bonusPuntos; }

    public BigDecimal getResultadoFinal() { return resultadoFinal; }
    public void setResultadoFinal(BigDecimal resultadoFinal) { this.resultadoFinal = resultadoFinal; }

    public int getActividadesCalificadas() { return actividadesCalificadas; }
    public void setActividadesCalificadas(int actividadesCalificadas) { this.actividadesCalificadas = actividadesCalificadas; }

    public int getActividadesTotales() { return actividadesTotales; }
    public void setActividadesTotales(int actividadesTotales) { this.actividadesTotales = actividadesTotales; }
}