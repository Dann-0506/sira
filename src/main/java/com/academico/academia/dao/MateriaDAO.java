package com.academico.academia.dao;

import com.academico.academia.model.Materia;
import com.academico.core.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MateriaDAO {

    // === Mapeo de ResultSet a Materia ===

    private Materia mapear(ResultSet rs) throws SQLException {
        return new Materia(
                rs.getInt("id"),
                rs.getString("clave"),
                rs.getString("nombre"),
                rs.getInt("total_unidades")
        );
    }


    // === Consulta ===

    public Optional<Materia> findById(int id) throws SQLException {
        String sql = "SELECT * FROM materia WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Materia> findAll() throws SQLException {
        String sql = "SELECT * FROM materia ORDER BY nombre";
        List<Materia> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }


    // === Escritura ===

    public Materia insertar(Materia m) throws SQLException {
        String sql = """
                INSERT INTO materia (clave, nombre, total_unidades)
                VALUES (?, ?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getClave());
            ps.setString(2, m.getNombre());
            ps.setInt(3, m.getTotalUnidades());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.setId(rs.getInt("id"));
            }
        }
        return m;
    }

    public List<String> insertarLote(List<Materia> materias) throws SQLException {
        List<String> duplicados = new ArrayList<>();
        String sql = """
                INSERT INTO materia (clave, nombre, total_unidades)
                VALUES (?, ?, ?)
                ON CONFLICT (clave) DO NOTHING
                """;
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Materia m : materias) {
                    ps.setString(1, m.getClave());
                    ps.setString(2, m.getNombre());
                    ps.setInt(3, m.getTotalUnidades());
                    if (ps.executeUpdate() == 0) duplicados.add(m.getClave());
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

    public void actualizar(Materia m) throws SQLException {
        String sql = """
                UPDATE materia
                SET clave = ?, nombre = ?, total_unidades = ?
                WHERE id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getClave());
            ps.setString(2, m.getNombre());
            ps.setInt(3, m.getTotalUnidades());
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }
}