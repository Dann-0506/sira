package com.academico.dao;

import com.academico.model.Inscripcion;
import com.academico.util.DatabaseManagerUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Inscripciones.
 * Gestiona la vinculación de alumnos a grupos y la aplicación de 
 * calificaciones finales manuales (overrides).
 */
public class InscripcionDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================
    
    private Inscripcion mapear(ResultSet rs) throws SQLException {
        Inscripcion i = new Inscripcion();
        i.setId(rs.getInt("id"));
        i.setAlumnoId(rs.getInt("alumno_id"));
        i.setGrupoId(rs.getInt("grupo_id"));
        
        Date fechaSql = rs.getDate("fecha");
        if (fechaSql != null) {
            i.setFecha(fechaSql.toLocalDate());
        }

        BigDecimal calc = rs.getBigDecimal("calificacion_final_calculada");
        i.setCalificacionFinalCalculada(rs.wasNull() ? null : calc);
        i.setEstadoAcademico(rs.getString("estado_academico"));

        BigDecimal override = rs.getBigDecimal("calificacion_final_override");
        i.setCalificacionFinalOverride(rs.wasNull() ? null : override);
        i.setOverrideJustificacion(rs.getString("override_justificacion"));

        try { i.setAlumnoNombre(rs.getString("alumno_nombre")); } catch (SQLException ignored) {}
        try { i.setAlumnoMatricula(rs.getString("alumno_matricula")); } catch (SQLException ignored) {}
        try { i.setGrupoClave(rs.getString("grupo_clave")); } catch (SQLException ignored) {}
        try { i.setMateriaNombre(rs.getString("materia_nombre")); } catch (SQLException ignored) {}
        try { i.setSemestre(rs.getString("semestre")); } catch (SQLException ignored) {}

        return i;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Inscripcion> findById(int id) throws SQLException {
        String sql = """
                SELECT i.*, 
                       u.nombre AS alumno_nombre, 
                       a.matricula AS alumno_matricula, 
                       g.clave AS grupo_clave, 
                       g.semestre AS semestre, 
                       m.nombre AS materia_nombre
                FROM inscripcion i
                JOIN alumno a ON i.alumno_id = a.id
                JOIN usuario u ON a.usuario_id = u.id
                JOIN grupo g ON i.grupo_id = g.id
                JOIN materia m ON g.materia_id = m.id
                WHERE i.id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapear(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Inscripcion> findByAlumnoYGrupo(int alumnoId, int grupoId) throws SQLException {
        String sql = "SELECT * FROM inscripcion WHERE alumno_id = ? AND grupo_id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
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
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Inscripcion> findAll() throws SQLException {
        String sql = """
                SELECT i.*, 
                       a.matricula AS alumno_matricula, 
                       u.nombre AS alumno_nombre,
                       g.clave AS grupo_clave,
                       g.semestre AS semestre,
                       m.nombre AS materia_nombre
                FROM inscripcion i
                JOIN alumno a ON a.id = i.alumno_id
                JOIN usuario u ON u.id = a.usuario_id
                JOIN grupo g ON g.id = i.grupo_id
                JOIN materia m ON m.id = g.materia_id
                ORDER BY i.fecha DESC, u.nombre
                """;
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public int contarPorGrupo(int grupoId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM inscripcion WHERE grupo_id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y TRANSACCIONES
    // ==========================================

    public Inscripcion insertar(Inscripcion i) throws SQLException {
        String sql = """
                INSERT INTO inscripcion (alumno_id, grupo_id, fecha)
                VALUES (?, ?, CURRENT_DATE)
                RETURNING id, fecha
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
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
        try (Connection conn = DatabaseManagerUtil.getConnection()) {
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

    public void guardarResultadosHistoricos(int inscripcionId, BigDecimal calculada, String estado) throws SQLException {
        String sql = """
                UPDATE inscripcion 
                SET calificacion_final_calculada = ?, 
                    estado_academico = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (calculada != null) {
                ps.setBigDecimal(1, calculada);
            } else {
                ps.setNull(1, Types.NUMERIC);
            }
            
            ps.setString(2, estado);
            ps.setInt(3, inscripcionId);
            
            ps.executeUpdate();
        }
    }

    // ==========================================
    // ACTUALIZACIÓN ACADÉMICA
    // ==========================================

    public void actualizarOverride(int inscripcionId, BigDecimal override, String justificacion) throws SQLException {
        String sql = """
                UPDATE inscripcion 
                SET calificacion_final_override = ?, override_justificacion = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
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
    
    // ==========================================
    // OPERACIONES DE ELIMINACIÓN
    // ==========================================

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM inscripcion WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}