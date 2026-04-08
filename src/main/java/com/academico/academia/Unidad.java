package com.academico.academia;

public class Unidad {
    private int id;
    private int numero;
    private String nombre;

    public Unidad() {}

    public Unidad(int id, int numero, String nombre) {
        this.id = id;
        this.numero = numero;
        this.nombre = nombre;
    }

    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public int getNumero()              { return numero; }
    public void setNumero(int numero)   { this.numero = numero; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Unidad{numero=" + numero + ", nombre='" + nombre + "'}";
    }
}