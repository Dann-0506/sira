package com.academico.service.individuals;

import com.academico.dao.ResultadoDAO;
import com.academico.model.Resultado;
import com.academico.util.DatabaseManagerUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la gestión de Calificaciones (Resultados).
 * Responsabilidad: Centralizar la lectura y escritura de calificaciones, 
 * validando reglas de negocio como el estado de la unidad y límites numéricos.
 */
public class ResultadoService {

    // === DEPENDENCIAS ===
    private final ResultadoDAO resultadoDAO;
    private final EstadoUnidadService estadoUnidadService;

    // === CONSTRUCTORES ===
    public ResultadoService() {
        this.resultadoDAO = new ResultadoDAO();
        this.estadoUnidadService = new EstadoUnidadService();
    }

    public ResultadoService(ResultadoDAO resultadoDAO, EstadoUnidadService estadoUnidadService) {
        this.resultadoDAO = resultadoDAO;
        this.estadoUnidadService = estadoUnidadService;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Resultado> buscarPorInscripcionYUnidad(int inscripcionId, int unidadId) throws Exception {
        if (inscripcionId <= 0 || unidadId <= 0) return List.of();
        
        try {
            return resultadoDAO.findByInscripcionYUnidad(inscripcionId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar las calificaciones del estudiante.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================

    public void guardarCalificacion(int inscripcionId, int grupoId, int unidadId, int actividadId, BigDecimal nota) throws Exception {
        // 1. Validaciones de Negocio (Rango permitido)
        if (nota != null && (nota.compareTo(BigDecimal.ZERO) < 0 || nota.compareTo(new BigDecimal("100")) > 0)) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 100.");
        }

        // 2. Validar que la unidad no esté cerrada
        estadoUnidadService.validarUnidadAbierta(grupoId, unidadId);

        // 3. Persistencia
        try {
            resultadoDAO.guardar(inscripcionId, actividadId, nota);
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar registrar la calificación.");
        }
    }

    public void guardarLote(int grupoId, int unidadId, List<Resultado> resultados) throws Exception {
        if (resultados == null || resultados.isEmpty()) return;

        // Validación preventiva de límites en lote
        for (Resultado r : resultados) {
            if (r.getCalificacion() != null && 
               (r.getCalificacion().compareTo(BigDecimal.ZERO) < 0 || r.getCalificacion().compareTo(new BigDecimal("100")) > 0)) {
                throw new IllegalArgumentException("Todas las calificaciones deben estar entre 0 y 100.");
            }
        }

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Bloqueo pesimista para concurrencia (Locking)
                String lockSql = "SELECT estado FROM estado_unidad WHERE grupo_id = ? AND unidad_id = ? FOR UPDATE";
                try (PreparedStatement ps = conn.prepareStatement(lockSql)) {
                    ps.setInt(1, grupoId);
                    ps.setInt(2, unidadId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && "CERRADA".equals(rs.getString("estado"))) {
                            throw new IllegalStateException("La unidad ha sido cerrada por otro proceso. Actualiza la página.");
                        }
                    }
                }

                // Persistencia masiva
                resultadoDAO.guardarLoteEnConexion(conn, resultados);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new Exception("Error al procesar la carga masiva: " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar guardar las calificaciones.");
        }
    }
}