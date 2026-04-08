package com.academico.academia;

import com.academico.core.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnidadDAO {

    // === Mapeo de ResultSet a Unidad ===

    private Unidad mapear(ResultSet rs) throws SQLException {
        Unidad u = new Unidad();
        u.setId(rs.getInt("id"));
        u.setNumero(rs.getInt("numero"));
        u.setNombre(rs.getString("nombre"));
        return u;
    }

    // === Consultas ===

    public Optional<Unidad> findById(int id) throws SQLException {
        String sql = "SELECT * FROM unidad WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
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
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Unidad> findAll() throws SQLException {
        String sql = "SELECT * FROM unidad ORDER BY numero";
        List<Unidad> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // === Escritura ===

    public Unidad insertar(Unidad u) throws SQLException {
        String sql = "INSERT INTO unidad (numero, nombre) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, u.getNumero());
            ps.setString(2, u.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getInt("id"));
                }
            }
        }
        return u;
    }

    // === Actualización ===

    public void actualizar(Unidad u) throws SQLException {
        String sql = "UPDATE unidad SET numero = ?, nombre = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, u.getNumero());
            ps.setString(2, u.getNombre());
            ps.setInt(3, u.getId());
            ps.executeUpdate();
        }
    }

    // === Eliminación ===

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM unidad WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}