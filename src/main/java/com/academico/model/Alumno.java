package com.academico.model;

/**
 * Representa a un estudiante en el sistema.
 * Está vinculado directamente a una cuenta de Usuario para el acceso.
 */
public class Alumno {
    
    // === ATRIBUTOS ===
    private int id;
    private Integer usuarioId;
    private String matricula;
    
    // Atributos heredados del Usuario (para facilitar la visualización en tablas)
    private String nombre;
    private String email;
    private boolean activo;

    // === CONSTRUCTORES ===
    public Alumno() {}

    public Alumno(int id, Integer usuarioId, String matricula, String nombre, String email) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.matricula = matricula;
        this.nombre = nombre;
        this.email = email;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "[" + matricula + "] " + nombre;
    }
}