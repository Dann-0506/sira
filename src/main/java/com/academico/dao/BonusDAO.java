package com.academico.dao;

import com.academico.model.Bonus;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Puntos Extra (Bonus).
 * Maneja la persistencia de las bonificaciones aplicadas a alumnos por unidad o materia.
 */
public class BonusDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Bonus mapear(ResultSet rs) throws SQLException {
        Bonus b = new Bonus();
        b.setId(rs.getInt("id"));
        b.setInscripcionId(rs.getInt("inscripcion_id"));
        
        int uid = rs.getInt("unidad_id");
        b.setUnidadId(rs.wasNull() ? null : uid);
        
        b.setTipo(rs.getString("tipo"));
        b.setPuntos(rs.getBigDecimal("puntos"));
        b.setJustificacion(rs.getString("justificacion"));
        b.setOtorgadoEn(rs.getTimestamp("otorgado_en").toLocalDateTime());
        return b;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Bonus> findByInscripcion(int inscripcionId) throws SQLException {
        String sql = """
                SELECT * FROM bonus
                WHERE inscripcion_id = ?
                ORDER BY otorgado_en
                """;
        List<Bonus> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Optional<Bonus> findByInscripcionYUnidad(int inscripcionId, int unidadId) throws SQLException {
        String sql = """
                SELECT * FROM bonus
                WHERE inscripcion_id = ?
                  AND unidad_id = ?
                  AND tipo = 'unidad'
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            ps.setInt(2, unidadId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Bonus> findBonusMateria(int inscripcionId) throws SQLException {
        String sql = """
                SELECT * FROM bonus
                WHERE inscripcion_id = ?
                  AND tipo = 'materia'
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inscripcionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ELIMINACIÓN
    // ==========================================

    public Bonus insertar(Bonus b) throws SQLException {
        String sql = """
                INSERT INTO bonus (inscripcion_id, unidad_id, tipo, puntos, justificacion)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, otorgado_en
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, b.getInscripcionId());
            
            // Protección contra nulos al insertar llaves foráneas
            if (b.getUnidadId() != null) {
                ps.setInt(2, b.getUnidadId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            
            ps.setString(3, b.getTipo());
            ps.setBigDecimal(4, b.getPuntos());
            ps.setString(5, b.getJustificacion());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    b.setId(rs.getInt("id"));
                    b.setOtorgadoEn(rs.getTimestamp("otorgado_en").toLocalDateTime());
                }
            }
        }
        return b;
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM bonus WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}