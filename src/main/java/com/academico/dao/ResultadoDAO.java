package com.academico.dao;

import com.academico.model.Resultado;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class ResultadoDAO {

    // === Mapeo de ResultSet a Resultado ===

    private Resultado mapear(ResultSet rs) throws SQLException {
        Resultado r = new Resultado();
        r.setId(rs.getInt("id"));
        r.setInscripcionId(rs.getInt("inscripcion_id"));
        r.setActividadGrupoId(rs.getInt("actividad_grupo_id"));
        BigDecimal cal = rs.getBigDecimal("calificacion");
        r.setCalificacion(rs.wasNull() ? null : cal);
        r.setModificadoEn(rs.getTimestamp("modificado_en").toLocalDateTime());
        try { r.setActividadNombre(rs.getString("actividad_nombre")); } catch (SQLException ignored) {}
        try { r.setPonderacion(rs.getBigDecimal("ponderacion")); }      catch (SQLException ignored) {}
        return r;
    }


    // === Consulta ===

    public List<Resultado> findByInscripcionYUnidad(int inscripcionId, int unidadId)
            throws SQLException {
        String sql = """
                SELECT r.*,
                       ag.nombre      AS actividad_nombre,
                       ag.ponderacion AS ponderacion
                FROM resultado r
                JOIN actividad_grupo ag ON ag.id = r.actividad_grupo_id
                WHERE r.inscripcion_id = ?
                  AND ag.unidad_id     = ?
                ORDER BY ag.id
                """;
        List<Resultado> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, unidadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Resultado> findByInscripcion(int inscripcionId) throws SQLException {
        String sql = """
                SELECT r.*,
                       ag.nombre      AS actividad_nombre,
                       ag.ponderacion AS ponderacion
                FROM resultado r
                JOIN actividad_grupo ag ON ag.id = r.actividad_grupo_id
                WHERE r.inscripcion_id = ?
                ORDER BY ag.unidad_id, ag.id
                """;
        List<Resultado> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }


    // === Escritura ===

    public void guardar(int inscripcionId, int actividadGrupoId,
                        java.math.BigDecimal calificacion) throws SQLException {
        String sql = """
                INSERT INTO resultado (inscripcion_id, actividad_grupo_id,
                                       calificacion, modificado_en)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (inscripcion_id, actividad_grupo_id)
                DO UPDATE SET calificacion  = EXCLUDED.calificacion,
                              modificado_en = NOW()
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, actividadGrupoId);
            if (calificacion != null) ps.setBigDecimal(3, calificacion);
            else ps.setNull(3, Types.NUMERIC);
            ps.executeUpdate();
        }
    }

    public void guardarLote(List<Resultado> resultados) throws SQLException {
        String sql = """
                INSERT INTO resultado (inscripcion_id, actividad_grupo_id,
                                       calificacion, modificado_en)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (inscripcion_id, actividad_grupo_id)
                DO UPDATE SET calificacion  = EXCLUDED.calificacion,
                              modificado_en = NOW()
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Resultado r : resultados) {
                    ps.setInt(1, r.getInscripcionId());
                    ps.setInt(2, r.getActividadGrupoId());
                    if (r.getCalificacion() != null)
                        ps.setBigDecimal(3, r.getCalificacion());
                    else
                        ps.setNull(3, Types.NUMERIC);
                    ps.addBatch();
                }
                ps.executeBatch();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void guardarLoteEnConexion(Connection conn, List<Resultado> resultados) throws SQLException {
        String sql = """
                INSERT INTO resultado 
                    (inscripcion_id, actividad_grupo_id, calificacion, modificado_en)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (inscripcion_id, actividad_grupo_id)
                DO UPDATE SET calificacion  = EXCLUDED.calificacion,
                            modificado_en = NOW()
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Resultado r : resultados) {
                ps.setInt(1, r.getInscripcionId());
                ps.setInt(2, r.getActividadGrupoId());
                if (r.getCalificacion() != null)
                    ps.setBigDecimal(3, r.getCalificacion());
                else
                    ps.setNull(3, Types.NUMERIC);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}