package com.academico.dao;

import com.academico.model.Usuario;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acceso a Datos (DAO) genérico para Administradores 
 * (Actúa sobre la tabla de usuarios filtrando por rol).
 */
public class AdminDAO {

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Usuario> findAllAdmins() throws SQLException {
        String sql = "SELECT * FROM usuario WHERE rol = 'admin' ORDER BY nombre";
        List<Usuario> lista = new ArrayList<>();
        
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setId(rs.getInt("id"));
                u.setNombre(rs.getString("nombre"));
                u.setEmail(rs.getString("email"));
                u.setActivo(rs.getBoolean("activo"));
                u.setRol(rs.getString("rol"));
                lista.add(u);
            }
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void crear(Usuario admin, String passwordHash) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, 'admin', true)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getNombre());
            ps.setString(2, admin.getEmail());
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }

    public void actualizar(Usuario admin) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, email = ? WHERE id = ? AND rol = 'admin'";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, admin.getNombre());
            ps.setString(2, admin.getEmail());
            ps.setInt(3, admin.getId());
            ps.executeUpdate();
        }
    }

    // ==========================================
    // ESTADO Y SEGURIDAD
    // ==========================================

    public void cambiarEstado(int id, boolean activo) throws SQLException {
        String sql = "UPDATE usuario SET activo = ? WHERE id = ? AND rol = 'admin'";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void actualizarPassword(int id, String passwordHash) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE id = ? AND rol = 'admin'";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id = ? AND rol = 'admin'";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}