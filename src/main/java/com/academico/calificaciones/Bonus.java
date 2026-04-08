package com.academico.calificaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Bonus {
    private int id;
    private int inscripcionId;
    private Integer unidadId; // nullable cuando tipo = 'materia'
    private String tipo;      // 'unidad' o 'materia'
    private BigDecimal puntos;
    private String justificacion;
    private LocalDateTime otorgadoEn;

    public Bonus() {}

    public Bonus(int id, int inscripcionId, Integer unidadId, String tipo, BigDecimal puntos, String justificacion) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.unidadId = unidadId;
        this.tipo = tipo;
        this.puntos = puntos;
        this.justificacion = justificacion;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getInscripcionId()                       { return inscripcionId; }
    public void setInscripcionId(int inscripcionId)     { this.inscripcionId = inscripcionId; }

    public Integer getUnidadId()                    { return unidadId; }
    public void setUnidadId(Integer unidadId)       { this.unidadId = unidadId; }

    public String getTipo()                 { return tipo; }
    public void setTipo(String tipo)        { this.tipo = tipo; }

    public BigDecimal getPuntos()                   { return puntos; }
    public void setPuntos(BigDecimal puntos)        { this.puntos = puntos; }

    public String getJustificacion()                        { return justificacion; }
    public void setJustificacion(String justificacion)      { this.justificacion = justificacion; }

    public LocalDateTime getOtorgadoEn()                        { return otorgadoEn; }
    public void setOtorgadoEn(LocalDateTime otorgadoEn)         { this.otorgadoEn = otorgadoEn; }

    public boolean esDeUnidad()     { return "unidad".equals(tipo); }
    public boolean esDeMateria()    { return "materia".equals(tipo); }

    @Override
    public String toString() {
        return "Bonus{tipo='" + tipo + "', puntos=" + puntos + "}";
    }
}