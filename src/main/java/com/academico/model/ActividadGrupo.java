package com.academico.model;

import java.math.BigDecimal;

public class ActividadGrupo {
    private int id;
    private int grupoId;
    private int unidadId;
    private String nombre;
    private BigDecimal ponderacion;
    private String unidadNombre;
    private int unidadNumero;

    public ActividadGrupo() {}

    public ActividadGrupo(int id, int grupoId, int unidadId,
                          String nombre, BigDecimal ponderacion) {
        this.id = id;
        this.grupoId = grupoId;
        this.unidadId = unidadId;
        this.nombre = nombre;
        this.ponderacion = ponderacion;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getGrupoId()                     { return grupoId; }
    public void setGrupoId(int grupoId)         { this.grupoId = grupoId; }

    public int getUnidadId()                    { return unidadId; }
    public void setUnidadId(int unidadId)       { this.unidadId = unidadId; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public BigDecimal getPonderacion()                      { return ponderacion; }
    public void setPonderacion(BigDecimal ponderacion)      { this.ponderacion = ponderacion; }

    public String getUnidadNombre()                         { return unidadNombre; }
    public void setUnidadNombre(String unidadNombre)        { this.unidadNombre = unidadNombre; }

    public int getUnidadNumero()                    { return unidadNumero; }
    public void setUnidadNumero(int unidadNumero)   { this.unidadNumero = unidadNumero; }

    @Override
    public String toString() {
        return "ActividadGrupo{nombre='" + nombre + "', ponderacion=" + ponderacion + "}";
    }
}