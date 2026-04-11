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
        
        try { g.setMateriaNombre(rs.getString("materia_nombre")); } catch (SQLException ignored) {}
        try { g.setMaestroNombre(rs.getString("maestro_nombre")); } catch (SQLException ignored) {}
        
        return g;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Grupo> findById(int id) throws SQLException {
        String sql = """
                SELECT g.*,
                       mat.nombre AS materia_nombre,
                       u.nombre   AS maestro_nombre
                FROM grupo g
                JOIN materia mat ON mat.id = g.materia_id
                JOIN maestro m   ON m.id   = g.maestro_id
                JOIN usuario u   ON u.id   = m.usuario_id
                WHERE g.id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
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
        String sql = """
                SELECT g.*,
                       mat.nombre AS materia_nombre,
                       u.nombre   AS maestro_nombre
                FROM grupo g
                JOIN materia mat ON mat.id = g.materia_id
                JOIN maestro m   ON m.id   = g.maestro_id
                JOIN usuario u   ON u.id   = m.usuario_id
                WHERE g.maestro_id = ?
                ORDER BY g.semestre DESC, g.clave
                """;
        List<Grupo> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maestroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y TRANSACCIONES
    // ==========================================

    public Grupo insertar(Grupo g) throws SQLException {
        String sql = """
                INSERT INTO grupo (materia_id, maestro_id, clave, semestre, activo)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, g.getMateriaId());
            ps.setInt(2, g.getMaestroId());
            ps.setString(3, g.getClave());
            ps.setString(4, g.getSemestre());
            ps.setBoolean(5, g.isActivo());
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
                INSERT INTO grupo (materia_id, maestro_id, clave, semestre, activo)
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT (clave) DO NOTHING
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
                    if (ps.executeUpdate() == 0) {
                        duplicados.add(g.getClave());
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