package com.academico.inscripciones;

import com.academico.core.db.DatabaseManager;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InscripcionDAO {

    // === Mapeo de ResultSet a Inscripcion ===
    
    private Inscripcion mapear(ResultSet rs) throws SQLException {
        Inscripcion i = new Inscripcion();
        i.setId(rs.getInt("id"));
        i.setAlumnoId(rs.getInt("alumno_id"));
        i.setGrupoId(rs.getInt("grupo_id"));
        
        Date fechaSql = rs.getDate("fecha");
        if (fechaSql != null) {
            i.setFecha(fechaSql.toLocalDate());
        }

        BigDecimal override = rs.getBigDecimal("calificacion_final_override");
        i.setCalificacionFinalOverride(rs.wasNull() ? null : override);
        i.setOverrideJustificacion(rs.getString("override_justificacion"));

        try { i.setAlumnoNombre(rs.getString("alumno_nombre")); } catch (SQLException ignored) {}
        try { i.setAlumnoMatricula(rs.getString("alumno_matricula")); } catch (SQLException ignored) {}
        try { i.setGrupoClave(rs.getString("grupo_clave")); } catch (SQLException ignored) {}

        return i;
    }


    // === Consultas ===

    public Optional<Inscripcion> findById(int id) throws SQLException {
        String sql = "SELECT * FROM inscripcion WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Inscripcion> findByAlumnoYGrupo(int alumnoId, int grupoId) throws SQLException {
        String sql = "SELECT * FROM inscripcion WHERE alumno_id = ? AND grupo_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            ps.setInt(2, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Inscripcion> findByGrupo(int grupoId) throws SQLException {
        String sql = """
                SELECT i.*, 
                       a.matricula AS alumno_matricula, 
                       u.nombre AS alumno_nombre
                FROM inscripcion i
                JOIN alumno a ON a.id = i.alumno_id
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE i.grupo_id = ?
                ORDER BY u.nombre NULLS LAST
                """;
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }


    // === Escritura ===

    public Inscripcion insertar(Inscripcion i) throws SQLException {
        String sql = """
                INSERT INTO inscripcion (alumno_id, grupo_id, fecha)
                VALUES (?, ?, CURRENT_DATE)
                RETURNING id, fecha
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, i.getAlumnoId());
            ps.setInt(2, i.getGrupoId());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    i.setId(rs.getInt("id"));
                    i.setFecha(rs.getDate("fecha").toLocalDate());
                }
            }
        }
        return i;
    }

    public List<String> insertarLote(List<Inscripcion> inscripciones) throws SQLException {
        List<String> duplicados = new ArrayList<>();
        String sql = """
                INSERT INTO inscripcion (alumno_id, grupo_id, fecha)
                VALUES (?, ?, CURRENT_DATE)
                ON CONFLICT (alumno_id, grupo_id) DO NOTHING
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Inscripcion i : inscripciones) {
                    ps.setInt(1, i.getAlumnoId());
                    ps.setInt(2, i.getGrupoId());
                    if (ps.executeUpdate() == 0) {
                        duplicados.add("Alumno ID: " + i.getAlumnoId() + " en Grupo ID: " + i.getGrupoId());
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        return duplicados;
    }

    // === Actualización ===

    public void actualizarOverride(int inscripcionId, BigDecimal override, String justificacion) throws SQLException {
        String sql = """
                UPDATE inscripcion 
                SET calificacion_final_override = ?, override_justificacion = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (override != null) {
                ps.setBigDecimal(1, override);
            } else {
                ps.setNull(1, Types.NUMERIC);
            }
            
            ps.setString(2, justificacion);
            ps.setInt(3, inscripcionId);
            
            ps.executeUpdate();
        }
    }
    
    // === Eliminación ===

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM inscripcion WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}