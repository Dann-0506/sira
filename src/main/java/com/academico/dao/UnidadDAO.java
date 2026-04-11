package com.academico.dao;

import com.academico.model.Unidad;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Unidades Académicas.
 * Controla el temario asociado a materias y su visualización en grupos.
 */
public class UnidadDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Unidad mapear(ResultSet rs) throws SQLException {
        Unidad u = new Unidad();
        u.setId(rs.getInt("id"));
        u.setMateriaId(rs.getInt("materia_id"));
        u.setNumero(rs.getInt("numero"));
        u.setNombre(rs.getString("nombre"));
        return u;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Unidad> findById(int id) throws SQLException {
        String sql = "SELECT * FROM unidad WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Unidad> findByGrupo(int grupoId) throws SQLException {
        String sql = """
                SELECT DISTINCT u.*
                FROM unidad u
                JOIN actividad_grupo ag ON ag.unidad_id = u.id
                WHERE ag.grupo_id = ?
                ORDER BY u.numero
                """;
        List<Unidad> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Unidad> findByMateria(int materiaId) throws SQLException {
        String sql = "SELECT * FROM unidad WHERE materia_id = ? ORDER BY numero";
        List<Unidad> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, materiaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Unidad> findAll() throws SQLException {
        String sql = "SELECT * FROM unidad ORDER BY numero";
        List<Unidad> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void insertar(Unidad u) throws SQLException {
        String sql = "INSERT INTO unidad (materia_id, numero, nombre) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, u.getMateriaId());
            ps.setInt(2, u.getNumero());
            ps.setString(3, u.getNombre());
            ps.executeUpdate();
        }
    }

    public void actualizar(Unidad u) throws SQLException {
        String sql = "UPDATE unidad SET materia_id = ?, numero = ?, nombre = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, u.getMateriaId());
            ps.setInt(2, u.getNumero());
            ps.setString(3, u.getNombre());
            ps.setInt(4, u.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarNombre(int id, String nombre) throws SQLException {
        String sql = "UPDATE unidad SET nombre = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // OPERACIONES DE ELIMINACIÓN
    // ==========================================

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM unidad WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}