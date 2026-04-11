package com.academico.model;

/**
 * Representa a un docente en el sistema.
 * Está vinculado a una cuenta de Usuario para gestionar su acceso y perfil.
 */
public class Maestro {
    
    // === ATRIBUTOS ===
    private int id;
    private int usuarioId;
    private String numEmpleado;
    
    // Atributos heredados del Usuario (Visualización)
    private String nombre;
    private String email;
    private boolean activo;

    // === CONSTRUCTORES ===
    public Maestro() {}

    public Maestro(int id, int usuarioId, String numEmpleado, String nombre, String email) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numEmpleado = numEmpleado;
        this.nombre = nombre;
        this.email = email;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNumEmpleado() { return numEmpleado; }
    public void setNumEmpleado(String numEmpleado) { this.numEmpleado = numEmpleado; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "[" + numEmpleado + "] " + nombre;
    }
}