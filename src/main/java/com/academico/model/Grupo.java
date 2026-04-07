package com.academico.model;

public class Grupo {
    private int id;
    private int materiaId;
    private int maestroId;
    private String clave;
    private String semestre;
    private boolean activo;
    private String materiaNombre;
    private String maestroNombre;

    public Grupo() {}

    public Grupo(int id, int materiaId, int maestroId,
                 String clave, String semestre, boolean activo) {
        this.id = id;
        this.materiaId = materiaId;
        this.maestroId = maestroId;
        this.clave = clave;
        this.semestre = semestre;
        this.activo = activo;
    }

    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public int getMateriaId()                       { return materiaId; }
    public void setMateriaId(int materiaId)         { this.materiaId = materiaId; }

    public int getMaestroId()                       { return maestroId; }
    public void setMaestroId(int maestroId)         { this.maestroId = maestroId; }

    public String getClave()                { return clave; }
    public void setClave(String clave)      { this.clave = clave; }

    public String getSemestre()                     { return semestre; }
    public void setSemestre(String semestre)        { this.semestre = semestre; }

    public boolean isActivo()               { return activo; }
    public void setActivo(boolean activo)   { this.activo = activo; }

    public String getMateriaNombre()                        { return materiaNombre; }
    public void setMateriaNombre(String materiaNombre)      { this.materiaNombre = materiaNombre; }

    public String getMaestroNombre()                        { return maestroNombre; }
    public void setMaestroNombre(String maestroNombre)      { this.maestroNombre = maestroNombre; }

    @Override
    public String toString() {
        return "Grupo{clave='" + clave + "', semestre='" + semestre + "'}";
    }
}