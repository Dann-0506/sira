package com.academico.calificaciones;

import com.academico.inscripciones.Inscripcion;
import com.academico.inscripciones.InscripcionDAO;
import com.academico.academia.EstadoUnidadService;
import com.academico.core.db.DatabaseManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public RegistroResultadoService(ResultadoDAO resultadoDAO, InscripcionDAO inscripcionDAO, EstadoUnidadService estadoUnidadService) {
        this.resultadoDAO = resultadoDAO;
        this.inscripcionDAO = inscripcionDAO;
        this.estadoUnidadService = estadoUnidadService;
    }

    public void guardarCalificacion(int inscripcionId, int grupoId, int unidadId, int actividadGrupoId, BigDecimal calificacion) throws SQLException {
        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);
        
        resultadoDAO.guardar(inscripcionId, actividadGrupoId, calificacion);
    }

    public void guardarLoteCalificaciones(int grupoId, int unidadId, List<Resultado> resultados) throws SQLException {
    try (Connection conn = DatabaseManager.getConnection()) {
        conn.setAutoCommit(false);
        try {
            String lockSql = """
                    SELECT estado FROM estado_unidad
                    WHERE grupo_id = ? AND unidad_id = ?
                    FOR UPDATE
                    """;
            try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                ps.setInt(1, grupoId);
                ps.setInt(2, unidadId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && "CERRADA".equals(rs.getString("estado"))) {
                        throw new IllegalStateException(
                            "No se puede guardar: la unidad está cerrada.");
                    }
                }
            }
            resultadoDAO.guardarLoteEnConexion(conn, resultados);
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}
    
    public void aplicarOverrideMateria(int inscripcionId, BigDecimal calificacionManual, String justificacion) throws SQLException {
        Inscripcion inscripcion = inscripcionDAO.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("La inscripción no existe."));
        
        inscripcionDAO.actualizarOverride(inscripcionId, calificacionManual, justificacion);
    }
}