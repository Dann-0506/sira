package com.academico.model;

import java.util.ArrayList;
import java.util.List;

public class Materia {
    private int id;
    private String clave;
    private String nombre;
    private int totalUnidades;
    private List<Unidad> unidades;

    public Materia() {
        this.unidades = new ArrayList<>();
    }

    public Materia(int id, String clave, String nombre, int totalUnidades) {
        this.id = id;
        this.clave = clave;
        this.nombre = nombre;
        this.totalUnidades = totalUnidades;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getClave()                { return clave; }
    public void setClave(String clave)      { this.clave = clave; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public int getTotalUnidades()                       { return totalUnidades; }
    public void setTotalUnidades(int totalUnidades)     { this.totalUnidades = totalUnidades; }

    public List<Unidad> getUnidades()                   { return unidades; }
    public void setUnidades(List<Unidad> unidades)      { this.unidades = unidades; }

    @Override
    public String toString() {
        return "Materia{clave='" + clave + "', nombre='" + nombre + "'}";
    }
}