package com.academico.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa puntos extra otorgados a un alumno.
 * Puede ser aplicado a una unidad específica o a la materia en general.
 */
public class Bonus {
    
    // === ATRIBUTOS ===
    private int id;
    private int inscripcionId;
    private Integer unidadId; // Nulo si es bonus de materia
    private String tipo;      // 'unidad' o 'materia'
    private BigDecimal puntos;
    private String justificacion;
    private LocalDateTime otorgadoEn;

    // === CONSTRUCTORES ===
    public Bonus() {}

    public Bonus(int id, int inscripcionId, Integer unidadId, String tipo, BigDecimal puntos, String justificacion) {
        this.id = id;
        this.inscripcionId = inscripcionId;
        this.unidadId = unidadId;
        this.tipo = tipo;
        this.puntos = puntos;
        this.justificacion = justificacion;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInscripcionId() { return inscripcionId; }
    public void setInscripcionId(int inscripcionId) { this.inscripcionId = inscripcionId; }

    public Integer getUnidadId() { return unidadId; }
    public void setUnidadId(Integer unidadId) { this.unidadId = unidadId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getPuntos() { return puntos; }
    public void setPuntos(BigDecimal puntos) { this.puntos = puntos; }

    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

    public LocalDateTime getOtorgadoEn() { return otorgadoEn; }
    public void setOtorgadoEn(LocalDateTime otorgadoEn) { this.otorgadoEn = otorgadoEn; }

    // === MÉTODOS DE CONVENIENCIA ===
    public boolean esDeUnidad() { return "unidad".equals(tipo); }
    public boolean esDeMateria() { return "materia".equals(tipo); }

    @Override
    public String toString() {
        return "+" + puntos + " pts (" + tipo + ")";
    }
}