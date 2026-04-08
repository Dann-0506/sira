package com.academico.inscripciones;

public class Alumno {
    private int id;
    private Integer usuarioId;
    private String matricula;
    private String nombre;
    private String email;

    public Alumno() {}

    public Alumno(int id, Integer usuarioId, String matricula, String nombre, String email) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.matricula = matricula;
        this.nombre = nombre;
        this.email = email;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public Integer getUsuarioId()                   { return usuarioId; }
    public void setUsuarioId(Integer usuarioId)     { this.usuarioId = usuarioId; }

    public String getMatricula()                    { return matricula; }
    public void setMatricula(String matricula)      { this.matricula = matricula; }

    public String getNombre()               { return nombre; }
    public void setNombre(String nombre)    { this.nombre = nombre; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    @Override
    public String toString() {
        return "Alumno{id=" + id + ", matricula='" + matricula + "', nombre='" + nombre + "'}";
    }
}