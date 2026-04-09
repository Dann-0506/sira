package com.academico.academia.dao;

import com.academico.academia.model.Maestro;
import com.academico.core.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaestroDAO {

    // === Mapeo de ResultSet a Maestro ===

    private Maestro mapear(ResultSet rs) throws SQLException {
        Maestro m = new Maestro();
        m.setId(rs.getInt("id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setNumEmpleado(rs.getString("num_empleado"));
        m.setNombre(rs.getString("nombre"));
        m.setEmail(rs.getString("email"));
        return m;
    }


    // === Consultas ===

    public Optional<Maestro> findById(int id) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Maestro> findByUsuarioId(int usuarioId) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.usuario_id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Maestro> findAll() throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                ORDER BY u.nombre
                """;
        List<Maestro> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }


    // === Escritura ===

    public Maestro insertar(Maestro m) throws SQLException {
        String sql = """
                INSERT INTO maestro (usuario_id, num_empleado)
                VALUES (?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getUsuarioId());
            if (m.getNumEmpleado() != null) ps.setString(2, m.getNumEmpleado());
            else ps.setNull(2, Types.VARCHAR);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                m.setId(rs.getInt("id"));
            }
        }
        return m;
    }
}