package com.academico.dao;

import com.academico.model.Grupo;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Grupos.
 * Gestiona la vinculación entre materias, maestros y la definición de semestres.
 */
public class GrupoDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Grupo mapear(ResultSet rs) throws SQLException {
        Grupo g = new Grupo();
        g.setId(rs.getInt("id"));
        g.setMateriaId(rs.getInt("materia_id"));
        g.setMaestroId(rs.getInt("maestro_id"));
        g.setClave(rs.getString("clave"));
        g.setSemestre(rs.getString("semestre"));
        g.setActivo(rs.getBoolean("activo"));
        g.setEstadoEvaluacion(rs.getString("estado_evaluacion"));
        g.setCalificacionMinimaAprobatoria(rs.getBigDecimal("calificacion_minima_aprobatoria"));
        g.setCalificacionMaxima(rs.getBigDecimal("calificacion_maxima"));
        
        try { g.setMateriaNombre(rs.getString("materia_nombre")); } catch (SQLException ignored) {}
        try { g.setMaestroNombre(rs.getString("maestro_nombre")); } catch (SQLException ignored) {}
        
        return g;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Grupo> findById(int id) throws SQLException {
        String sql = """
                SELECT g.*, m.nombre AS materia_nombre, u.nombre AS maestro_nombre 
                FROM grupo g
                JOIN materia m ON g.materia_id = m.id
                JOIN maestro ma ON g.maestro_id = ma.id
                JOIN usuario u ON ma.usuario_id = u.id
                WHERE g.id = ?
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

    public Optional<Grupo> findByClave(String clave) throws SQLException {
        String sql = """
                SELECT g.*,
                       mat.nombre AS materia_nombre,
                       u.nombre   AS maestro_nombre
                FROM grupo g
                JOIN materia mat ON mat.id = g.materia_id
                JOIN maestro m   ON m.id   = g.maestro_id
                JOIN usuario u   ON u.id   = m.usuario_id
                WHERE g.clave = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Grupo> findByClaveYSemestre(String clave, String semestre) throws SQLException {
        String sql = """
                SELECT g.*, mat.nombre AS materia_nombre, u.nombre AS maestro_nombre
                FROM grupo g
                JOIN materia mat ON mat.id = g.materia_id
                JOIN maestro m   ON m.id   = g.maestro_id
                JOIN usuario u   ON u.id   = m.usuario_id
                WHERE g.clave = ? AND g.semestre = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, semestre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Grupo> findAll() throws SQLException {
        String sql = """
                SELECT g.*,
                       mat.nombre AS materia_nombre,
                       u.nombre   AS maestro_nombre
                FROM grupo g
                JOIN materia mat ON mat.id = g.materia_id
                JOIN maestro m   ON m.id   = g.maestro_id
                JOIN usuario u   ON u.id   = m.usuario_id
                ORDER BY g.semestre DESC, g.clave
                """;
        List<Grupo> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Grupo> findByMaestro(int maestroId) throws SQLException {
        List<Grupo> grupos = new ArrayList<>();
        // Ajustamos el SQL para incluir los nombres de las materias (Joins)
        String sql = """
                SELECT g.*, m.nombre as materia_nombre 
                FROM grupo g
                JOIN materia m ON g.materia_id = m.id
                WHERE g.maestro_id = ? AND g.estado_evaluacion = 'ABIERTO'
                ORDER BY g.semestre DESC
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maestroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grupos.add(mapear(rs));
                }
            }
        }
        return grupos;
    }

    public List<Grupo> findByAlumno(int alumnoId) throws SQLException {
        String sql = """
                SELECT g.*, m.nombre as materia_nombre, u.nombre as maestro_nombre,
                       (SELECT COUNT(*) FROM inscripcion WHERE grupo_id = g.id) as total_alumnos
                FROM grupo g
                JOIN materia m ON m.id = g.materia_id
                LEFT JOIN maestro ma ON ma.id = g.maestro_id
                LEFT JOIN usuario u ON u.id = ma.usuario_id
                JOIN inscripcion i ON i.grupo_id = g.id
                WHERE i.alumno_id = ? 
                ORDER BY g.semestre DESC, m.nombre ASC
                """;
        List<Grupo> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alumnoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Grupo g = mapear(rs); 
                    g.setMateriaNombre(rs.getString("materia_nombre"));
                    g.setMaestroNombre(rs.getString("maestro_nombre"));
                    try { 
                        g.setTotalAlumnos(rs.getInt("total_alumnos")); 
                    } catch (Exception ignored) {} 
                    lista.add(g);
                }
            }
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y TRANSACCIONES
    // ==========================================

    public Grupo insertar(Grupo g) throws SQLException {
        String sql = """
                INSERT INTO grupo (materia_id, maestro_id, clave, semestre, activo, 
                                calificacion_minima_aprobatoria, calificacion_maxima)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getMateriaId());
            ps.setInt(2, g.getMaestroId());
            ps.setString(3, g.getClave());
            ps.setString(4, g.getSemestre());
            ps.setBoolean(5, g.isActivo());

            ps.setBigDecimal(6, g.getCalificacionMinimaAprobatoria());
            ps.setBigDecimal(7, g.getCalificacionMaxima());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    g.setId(rs.getInt("id"));
                }
            }
        }
        return g;
    }

    public List<String> insertarLote(List<Grupo> grupos) throws SQLException {
        List<String> duplicados = new ArrayList<>();

        String sql = """
                INSERT INTO grupo (materia_id, maestro_id, clave, semestre, activo, 
                                calificacion_minima_aprobatoria, calificacion_maxima)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (clave, materia_id, semestre) DO NOTHING
                """;
                
        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Grupo g : grupos) {
                    ps.setInt(1, g.getMateriaId());
                    ps.setInt(2, g.getMaestroId());
                    ps.setString(3, g.getClave());
                    ps.setString(4, g.getSemestre());
                    ps.setBoolean(5, g.isActivo());
                    ps.setBigDecimal(6, g.getCalificacionMinimaAprobatoria());
                    ps.setBigDecimal(7, g.getCalificacionMaxima());
                    
                    // Si no se insertó nada (por el DO NOTHING), lo marcamos como duplicado
                    if (ps.executeUpdate() == 0) {
                        duplicados.add(g.getClave() + " (" + g.getSemestre() + ")");
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

    // ==========================================
    // OPERACIONES DE ACTUALIZACIÓN
    // ==========================================

    public void actualizar(Grupo g) throws SQLException {
        String sql = """
                UPDATE grupo
                SET materia_id = ?, maestro_id = ?, clave = ?,
                    semestre = ?, activo = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getMateriaId());
            ps.setInt(2, g.getMaestroId());
            ps.setString(3, g.getClave());
            ps.setString(4, g.getSemestre());
            ps.setBoolean(5, g.isActivo());
            ps.setInt(6, g.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarEstadoEvaluacion(int id, String estado) throws SQLException {
        String sql = "UPDATE grupo SET estado_evaluacion = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizarEstadoActa(int id, String estadoEvaluacion, boolean activo) throws SQLException {
        String sql = "UPDATE grupo SET estado_evaluacion = ?, activo = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estadoEvaluacion);
            ps.setBoolean(2, activo);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // OPERACIONES DE ELIMINACIÓN
    // ==========================================

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM grupo WHERE id = ?";
        
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}