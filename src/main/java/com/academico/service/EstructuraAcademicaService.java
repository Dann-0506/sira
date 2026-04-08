package com.academico.service;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;

import java.math.BigDecimal;
import java.sql.SQLException;

public class EstructuraAcademicaService {

    private final ActividadGrupoDAO actividadDAO;

    public EstructuraAcademicaService() {
        this.actividadDAO = new ActividadGrupoDAO();
    }

    public boolean puedeAgregarActividad(int grupoId, int unidadId, BigDecimal nuevaPonderacion) throws SQLException {
        BigDecimal sumaActual = actividadDAO.sumaPonderaciones(grupoId, unidadId);
        
        BigDecimal sumaSimulada = sumaActual.add(nuevaPonderacion);
        
        return sumaSimulada.compareTo(new BigDecimal("100.00")) <= 0;
    }

    public ActividadGrupo guardarActividad(ActividadGrupo actividad) throws SQLException {
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
}