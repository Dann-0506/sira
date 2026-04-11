package com.academico.model;

/**
 * Representa una cuenta de acceso al sistema.
 * Gestiona las credenciales, el rol (admin, maestro, alumno) y el estado de la cuenta.
 */
public class Usuario {
    
    // === IDENTIDAD Y PERFIL ===
    private int id;
    private String nombre;
    private String email;
    private String rol;

    // === SEGURIDAD Y ESTADO ===
    private String passwordHash;
    private boolean activo;
    private boolean requiereCambioPassword = false;

    // === CONSTRUCTORES ===
    public Usuario() {}

    public Usuario(int id, String nombre, String email, String rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    // === GETTERS Y SETTERS ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public boolean isRequiereCambioPassword() { return requiereCambioPassword; }
    public void setRequiereCambioPassword(boolean requiereCambioPassword) { this.requiereCambioPassword = requiereCambioPassword; }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}