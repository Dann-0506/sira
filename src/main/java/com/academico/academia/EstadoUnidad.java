package com.academico.academia;

import java.time.LocalDateTime;

public class EstadoUnidad {
    private int id;
    private int grupoId;
    private int unidadId;
    private String estado;
    private LocalDateTime actualizadoEn;

    public EstadoUnidad() {}

    public EstadoUnidad(int id, int grupoId, int unidadId, String estado, LocalDateTime actualizadoEn) {
        this.id = id;
        this.grupoId = grupoId;
        this.unidadId = unidadId;
        this.estado = estado;
        this.actualizadoEn = actualizadoEn;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGrupoId() { return grupoId; }
    public void setGrupoId(int grupoId) { this.grupoId = grupoId; }

    public int getUnidadId() { return unidadId; }
    public void setUnidadId(int unidadId) { this.unidadId = unidadId; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) {this.estado = estado; }

    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }


    public boolean isCerrada() {
        return "CERRADA".equals(this.estado);
    }
}
