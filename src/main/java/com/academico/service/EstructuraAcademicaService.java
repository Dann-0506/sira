package com.academico.service;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;

import java.math.BigDecimal;
import java.sql.SQLException;

public class EstructuraAcademicaService {

    private final ActividadGrupoDAO actividadDAO;
    private final EstadoUnidadService estadoUnidadService;

    public EstructuraAcademicaService() {
        this.actividadDAO = new ActividadGrupoDAO();
        this.estadoUnidadService = new EstadoUnidadService();
    }

    // === Validaciones matemáticas ===

    public boolean puedeAgregarActividad(int grupoId, int unidadId, BigDecimal nuevaPonderacion) throws SQLException {
        BigDecimal sumaActual = actividadDAO.sumaPonderaciones(grupoId, unidadId);
        
        BigDecimal sumaSimulada = sumaActual.add(nuevaPonderacion);
        
        return sumaSimulada.compareTo(new BigDecimal("100.00")) <= 0;
    }

    // === Escritura y modificación ===

    public ActividadGrupo guardarActividad(ActividadGrupo actividad) throws SQLException {
        estadoUnidadService.validarUnidadAbierta(actividad.getGrupoId(), actividad.getUnidadId());

        boolean esValido = puedeAgregarActividad(
                actividad.getGrupoId(), 
                actividad.getUnidadId(), 
                actividad.getPonderacion()
        );

        if (!esValido) {
            throw new IllegalArgumentException("Error: La suma de ponderaciones de la unidad excedería el 100%.");
        }

        return actividadDAO.insertar(actividad);
    }

    public ActividadGrupo actualizarActividad(ActividadGrupo actividad) throws SQLException {
        estadoUnidadService.validarUnidadAbierta(actividad.getGrupoId(), actividad.getUnidadId());

        BigDecimal sumaActual = actividadDAO.sumaPonderaciones(
                actividad.getGrupoId(), actividad.getUnidadId());
        
        ActividadGrupo actual = actividadDAO.findById(actividad.getId())
                .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
        
        BigDecimal sumaExcluyendoActual = sumaActual.subtract(actual.getPonderacion());
        BigDecimal sumaSimulada = sumaExcluyendoActual.add(actividad.getPonderacion());
        
        if (sumaSimulada.compareTo(new BigDecimal("100.00")) > 0) {
            throw new IllegalArgumentException(
                "La suma de ponderaciones excedería el 100%.");
        }
        
        actividadDAO.actualizar(actividad);
        return actividad;
    }

    public void eliminarActividad(int actividadId) throws SQLException {
        ActividadGrupo actual = actividadDAO.findById(actividadId)
             .orElseThrow(() -> new IllegalArgumentException("Actividad no encontrada"));
        
        estadoUnidadService.validarUnidadAbierta(actual.getGrupoId(), actual.getUnidadId());
        
        actividadDAO.eliminar(actividadId);
    }
}