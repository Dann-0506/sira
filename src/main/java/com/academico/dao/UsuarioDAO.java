package com.academico.dao;

import com.academico.model.Usuario;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para la entidad Usuario.
 * Centraliza la gestión de perfiles, autenticación y estados de acceso global.
 */
public class UsuarioDAO {
    
    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("activo"));
        return u;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Usuario> findById(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Usuario> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY nombre";
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapear(rs));
            }
        }
        return usuarios;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public Usuario insertar(Usuario u) throws SQLException {
        String sql = """
                INSERT INTO usuario (nombre, email, password_hash, rol, activo)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getPasswordHash());
            ps.setString(4, u.getRol());
            ps.setBoolean(5, u.isActivo());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    u.setId(rs.getInt("id"));
                }
            }
        }
        return u;
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, email = ?, activo = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getEmail());
            ps.setBoolean(3, u.isActivo());
            ps.setInt(4, u.getId());
            ps.executeUpdate();
        }
    }

    public void actualizarPassword(int id, String nuevoHash) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nuevoHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Lógica especial de actualización de perfil: Incluye el cambio de contraseña 
     * solo si el campo no está vacío.
     */
    public void actualizarPerfil(int id, String nombre, String email, String passwordHash) throws SQLException {
        boolean cambiaPass = passwordHash != null && !passwordHash.isEmpty();
        String sql = cambiaPass 
                ? "UPDATE usuario SET nombre = ?, email = ?, password_hash = ? WHERE id = ?"
                : "UPDATE usuario SET nombre = ?, email = ? WHERE id = ?";
        
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setString(1, nombre);
            ps.setString(2, email);
            
            if (cambiaPass) {
                ps.setString(3, passwordHash);
                ps.setInt(4, id);
            } else {
                ps.setInt(3, id);
            }
            ps.executeUpdate();
        }
    }

    public void desactivar(int id) throws SQLException {
        String sql = "UPDATE usuario SET activo = FALSE WHERE id = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}