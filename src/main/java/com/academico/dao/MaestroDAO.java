package com.academico.dao;

import com.academico.model.Maestro;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Maestros.
 * Coordina transacciones complejas entre las tablas 'maestro' y 'usuario'.
 */
public class MaestroDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Maestro mapear(ResultSet rs) throws SQLException {
        Maestro m = new Maestro();
        m.setId(rs.getInt("id"));
        m.setUsuarioId(rs.getInt("usuario_id"));
        m.setNumEmpleado(rs.getString("num_empleado"));
        m.setNombre(rs.getString("nombre"));
        m.setEmail(rs.getString("email"));
        m.setActivo(rs.getBoolean("activo"));
        return m;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Maestro> findById(int id) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email, u.activo
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Maestro> findByUsuarioId(int usuarioId) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email, u.activo
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.usuario_id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Maestro> findByNumEmpleado(String numEmpleado) throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email, u.activo
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                WHERE m.num_empleado = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Maestro> findAll() throws SQLException {
        String sql = """
                SELECT m.*, u.nombre, u.email, u.activo
                FROM maestro m
                JOIN usuario u ON u.id = m.usuario_id
                ORDER BY u.nombre
                """;
        List<Maestro> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y TRANSACCIONES
    // ==========================================

    public Maestro insertar(Maestro m) throws SQLException {
        String sql = """
                INSERT INTO maestro (usuario_id, num_empleado)
                VALUES (?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, m.getUsuarioId());
            if (m.getNumEmpleado() != null) ps.setString(2, m.getNumEmpleado());
            else ps.setNull(2, Types.VARCHAR);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    m.setId(rs.getInt("id"));
                }
            }
        }
        return m;
    }

    public void crear(Maestro m, String passwordHash) throws SQLException {
        String sqlUsuario = """
                INSERT INTO usuario (nombre, email, password_hash, rol, activo)
                VALUES (?, ?, ?, 'maestro', true) RETURNING id
                """;
        String sqlMaestro = """
                INSERT INTO maestro (usuario_id, num_empleado)
                VALUES (?, ?)
                """;

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                int nuevoUsuarioId = -1;

                // 1. Insertar datos en la tabla Usuario
                try (PreparedStatement psU = conn.prepareStatement(sqlUsuario)) {
                    psU.setString(1, m.getNombre());
                    psU.setString(2, m.getEmail());
                    psU.setString(3, passwordHash); 
                    try (ResultSet rs = psU.executeQuery()) {
                        if (rs.next()) nuevoUsuarioId = rs.getInt(1);
                    }
                }

                // 2. Insertar datos en la tabla Maestro
                try (PreparedStatement psM = conn.prepareStatement(sqlMaestro)) {
                    psM.setInt(1, nuevoUsuarioId);
                    psM.setString(2, m.getNumEmpleado());
                    psM.executeUpdate();
                }

                conn.commit(); 
            } catch (SQLException e) {
                conn.rollback(); 
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // OPERACIONES DE ACTUALIZACIÓN
    // ==========================================

    public void actualizar(Maestro m) throws SQLException {
        String sqlUsuario = "UPDATE usuario SET nombre = ?, email = ? WHERE id = ?";
        String sqlMaestro = "UPDATE maestro SET num_empleado = ? WHERE id = ?";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                try (PreparedStatement psU = conn.prepareStatement(sqlUsuario)) {
                    psU.setString(1, m.getNombre());
                    psU.setString(2, m.getEmail());
                    psU.setInt(3, m.getUsuarioId());
                    psU.executeUpdate();
                }
                try (PreparedStatement psM = conn.prepareStatement(sqlMaestro)) {
                    psM.setString(1, m.getNumEmpleado());
                    psM.setInt(2, m.getId());
                    psM.executeUpdate();
                }
                conn.commit(); 
            } catch (SQLException e) {
                conn.rollback(); 
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // ==========================================
    // SEGURIDAD, ESTADO Y ELIMINACIÓN
    // ==========================================

    public void cambiarEstado(int maestroId, boolean estado) throws SQLException {
        String sql = "UPDATE usuario SET activo = ? WHERE id = (SELECT usuario_id FROM maestro WHERE id = ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, estado);
            ps.setInt(2, maestroId);
            ps.executeUpdate();
        }
    }
    
    public void actualizarPassword(int maestroId, String passwordHash) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE id = (SELECT usuario_id FROM maestro WHERE id = ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, maestroId);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idMaestro) throws SQLException {
        String sqlGetUsuario = "SELECT usuario_id FROM maestro WHERE id = ?";
        String sqlDelMaestro = "DELETE FROM maestro WHERE id = ?";
        String sqlDelUsuario = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // a) Obtener el ID del usuario vinculado
                Integer usuarioId = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlGetUsuario)) {
                    ps.setInt(1, idMaestro);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) usuarioId = rs.getInt("usuario_id");
                    }
                }

                // b) Eliminar al Maestro (Lanzará error si tiene grupos asignados por Foreign Key)
                try (PreparedStatement ps = conn.prepareStatement(sqlDelMaestro)) {
                    ps.setInt(1, idMaestro);
                    ps.executeUpdate();
                }

                // c) Eliminar Usuario (Solo si se pudo borrar el maestro)
                if (usuarioId != null) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlDelUsuario)) {
                        ps.setInt(1, usuarioId);
                        ps.executeUpdate();
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
    }
}