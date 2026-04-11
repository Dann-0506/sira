package com.academico.model;

/**
 * Representa una unidad académica perteneciente al temario de una materia.
 */
public class Unidad {
    
    // === ATRIBUTOS ===
    private int id;
    private int materiaId;
    private int numero;
    private String nombre;

    // === CONSTRUCTORES ===
    public Unidad() {}

    public Unidad(int id, int materiaId, int numero, String nombre) {
        this.id = id;
        this.materiaId = materiaId;
        this.numero = numero;
        this.nombre = nombre;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMateriaId() { return materiaId; }
    public void setMateriaId(int materiaId) { this.materiaId = materiaId; }

    public int getNumero() { return numero; }
    public void setNumero(int numero) { this.numero = numero; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return "Unidad " + numero + ": " + nombre;
    }
}