package com.academico.dao;

import com.academico.db.DatabaseManager;
import com.academico.model.ActividadGrupo;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ActividadGrupoDAO {

    // === Mapeo de ResultSet a ActividadGrupo ===

    private ActividadGrupo mapear(ResultSet rs) throws SQLException {
        ActividadGrupo a = new ActividadGrupo();
        a.setId(rs.getInt("id"));
        a.setGrupoId(rs.getInt("grupo_id"));
        a.setUnidadId(rs.getInt("unidad_id"));
        a.setNombre(rs.getString("nombre"));
        a.setPonderacion(rs.getBigDecimal("ponderacion"));
        try { a.setUnidadNumero(rs.getInt("unidad_numero")); } catch (SQLException ignored) {}
        try { a.setUnidadNombre(rs.getString("unidad_nombre")); } catch (SQLException ignored) {}
        return a;
    }


    // === Consulta ===

    public Optional<ActividadGrupo> findById(int id) throws SQLException {
        String sql = """
                SELECT ag.*,
                    u.numero AS unidad_numero,
                    u.nombre AS unidad_nombre
                FROM actividad_grupo ag
                JOIN unidad u ON u.id = ag.unidad_id
                WHERE ag.id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<ActividadGrupo> findByGrupoYUnidad(int grupoId, int unidadId)
            throws SQLException {
        String sql = """
                SELECT ag.*,
                       u.numero AS unidad_numero,
                       u.nombre AS unidad_nombre
                FROM actividad_grupo ag
                JOIN unidad u ON u.id = ag.unidad_id
                WHERE ag.grupo_id = ? AND ag.unidad_id = ?
                ORDER BY ag.id
                """;
        List<ActividadGrupo> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            ps.setInt(2, unidadId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<ActividadGrupo> findByGrupo(int grupoId) throws SQLException {
        String sql = """
                SELECT ag.*,
                       u.numero AS unidad_numero,
                       u.nombre AS unidad_nombre
                FROM actividad_grupo ag
                JOIN unidad u ON u.id = ag.unidad_id
                WHERE ag.grupo_id = ?
                ORDER BY u.numero, ag.id
                """;
        List<ActividadGrupo> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    /**
     * Suma actual de ponderaciones para un grupo/unidad.
     * Usado para validar en tiempo real antes de guardar.
     */
    public BigDecimal sumaPonderaciones(int grupoId, int unidadId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(ponderacion), 0)
                FROM actividad_grupo
                WHERE grupo_id = ? AND unidad_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            ps.setInt(2, unidadId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }


    // === Escritura ===

    public ActividadGrupo insertar(ActividadGrupo a) throws SQLException {
        String sql = """
                INSERT INTO actividad_grupo (grupo_id, unidad_id, nombre, ponderacion)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, a.getGrupoId());
            ps.setInt(2, a.getUnidadId());
            ps.setString(3, a.getNombre());
            ps.setBigDecimal(4, a.getPonderacion());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                a.setId(rs.getInt("id"));
            }
        }
        return a;
    }


    // === Actualización ===

    public void actualizar(ActividadGrupo a) throws SQLException {
        String sql = """
                UPDATE actividad_grupo
                SET nombre = ?, ponderacion = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getNombre());
            ps.setBigDecimal(2, a.getPonderacion());
            ps.setInt(3, a.getId());
            ps.executeUpdate();
        }
    }


    // === Eliminación ===

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM actividad_grupo WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}