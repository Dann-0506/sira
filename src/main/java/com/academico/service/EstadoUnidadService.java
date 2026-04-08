package com.academico.service;

import com.academico.dao.EstadoUnidadDAO;
import com.academico.model.EstadoUnidad;

import java.sql.SQLException;


public class EstadoUnidadService {
    private final EstadoUnidadDAO estadoUnidadDAO;

    public EstadoUnidadService() {
        this.estadoUnidadDAO = new EstadoUnidadDAO();
    }

    public void validarUnidadAbierta(int grupoId, int unidadId) throws SQLException {
        EstadoUnidad estado = estadoUnidadDAO.findByGrupoYUnidad(grupoId, unidadId);

        if (estado.isCerrada()) {
            throw new IllegalStateException("Acción no permitida: Unidad cerrada.");
        }
    }

    public EstadoUnidad obtenerEstado(int grupoId, int unidadId) throws SQLException {
        return estadoUnidadDAO.findByGrupoYUnidad(grupoId, unidadId);
    }

    public void cerrarUnidad(int grupoId, int unidadId) throws SQLException {
        estadoUnidadDAO.guardarEstado(grupoId, unidadId, "CERRADA");
    }

    public void abrirUnidad(int grupoId, int unidadId) throws SQLException {
        estadoUnidadDAO.guardarEstado(grupoId, unidadId, "ABIERTA");
    }
}
