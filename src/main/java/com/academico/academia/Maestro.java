package com.academico.academia;

public class Maestro {
    private int id;
    private int usuarioId;
    private String numEmpleado;
    private String nombre;
    private String email;

    public Maestro() {}

    public Maestro(int id, int usuarioId, String numEmpleado, String nombre, String email) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.numEmpleado = numEmpleado;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getUsuarioId()                       { return usuarioId; }
    public void setUsuarioId(int usuarioId)         { this.usuarioId = usuarioId; }

    public String getNumEmpleado()                      { return numEmpleado; }
    public void setNumEmpleado(String numEmpleado)      { this.numEmpleado = numEmpleado; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    @Override
    public String toString() {
        return "Maestro{id=" + id + ", nombre='" + nombre + "'}";
    }
}