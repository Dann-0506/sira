package com.academico.dao;

import com.academico.model.Materia;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Materias.
 * Gestiona el catálogo de asignaturas y la definición del total de unidades por materia.
 */
public class MateriaDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Materia mapear(ResultSet rs) throws SQLException {
        return new Materia(
                rs.getInt("id"),
                rs.getString("clave"),
                rs.getString("nombre"),
                rs.getInt("total_unidades")
        );
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Materia> findById(int id) throws SQLException {
        String sql = "SELECT * FROM materia WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
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

    public Materia insertar(Materia m) throws SQLException {
        String sql = "INSERT INTO materia (clave, nombre, total_unidades) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, m.getClave());
            ps.setString(2, m.getNombre());
            ps.setInt(3, m.getTotalUnidades());
            ps.executeUpdate();
            
            // Recuperamos el ID generado automáticamente por PostgreSQL
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setId(rs.getInt(1));
                }
            }
        }
        return m;
    }
    
    public void actualizar(Materia m) throws SQLException {
        String sql = "UPDATE materia SET clave = ?, nombre = ?, total_unidades = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getClave());
            ps.setString(2, m.getNombre());
            ps.setInt(3, m.getTotalUnidades());
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }

    // ==========================================
    // OPERACIONES DE ELIMINACIÓN
    // ==========================================

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM materia WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}