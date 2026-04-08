package com.academico.service;

import com.academico.dao.ResultadoDAO;
import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;
import com.academico.model.Resultado;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class RegistroResultadoService {

    private final ResultadoDAO resultadoDAO;
    private final InscripcionDAO inscripcionDAO;
    private final EstadoUnidadService estadoUnidadService;

    public RegistroResultadoService() {
        this.resultadoDAO = new ResultadoDAO();
        this.inscripcionDAO = new InscripcionDAO();
        this.estadoUnidadService = new EstadoUnidadService();
    }

    public void guardarCalificacion(int inscripcionId, int grupoId, int unidadId, int actividadGrupoId, BigDecimal calificacion) throws SQLException {
        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);
        
        resultadoDAO.guardar(inscripcionId, actividadGrupoId, calificacion);
    }

    public void guardarLoteCalificaciones(int grupoId, int unidadId, List<Resultado> resultados) throws SQLException {
        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);
        
        resultadoDAO.guardarLote(resultados);
    }
    
    public void aplicarOverrideMateria(int inscripcionId, BigDecimal calificacionManual, String justificacion) throws SQLException {
        Inscripcion inscripcion = inscripcionDAO.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción no existe."));
        
        inscripcionDAO.actualizarOverride(inscripcionId, calificacionManual, justificacion);
    }
}