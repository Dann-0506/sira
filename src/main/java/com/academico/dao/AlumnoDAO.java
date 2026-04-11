package com.academico.dao;

import com.academico.model.Alumno;
import com.academico.util.DatabaseManagerUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Objeto de Acceso a Datos (DAO) para Alumnos.
 * Coordina transacciones complejas entre la tabla 'alumno' y 'usuario'.
 */
public class AlumnoDAO {

    // ==========================================
    // MAPEO DE RESULTADOS
    // ==========================================

    private Alumno mapear(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setId(rs.getInt("id"));
        
        int uid = rs.getInt("usuario_id");
        a.setUsuarioId(rs.wasNull() ? null : uid);
        
        a.setMatricula(rs.getString("matricula"));
        a.setNombre(rs.getString("nombre"));
        a.setEmail(rs.getString("email"));
        a.setActivo(rs.getBoolean("activo"));
        return a;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Alumno> findById(int id) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email, u.activo
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE a.id = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Alumno> findByMatricula(String matricula) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email, u.activo
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE a.matricula = ?
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Alumno> findAll() throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email, u.activo
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                ORDER BY u.nombre
                """;
        List<Alumno> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Alumno> obtenerTodos() throws SQLException {
        List<Alumno> lista = new ArrayList<>();
        String sql = """
                SELECT a.*, u.nombre, u.email, u.activo
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                ORDER BY a.id ASC
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Alumno> findByGrupo(int grupoId) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email, u.activo
                FROM alumno a
                JOIN inscripcion i ON i.alumno_id = a.id
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE i.grupo_id = ?
                ORDER BY u.nombre
                """;
        List<Alumno> lista = new ArrayList<>();
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y TRANSACCIONES
    // ==========================================

    public Alumno insertar(Alumno a) throws SQLException {
        String sql = """
                INSERT INTO alumno (usuario_id, matricula)
                VALUES (?, ?) RETURNING id
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (a.getUsuarioId() != null) ps.setInt(1, a.getUsuarioId());
            else ps.setNull(1, Types.INTEGER);
            ps.setString(2, a.getMatricula());
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                a.setId(rs.getInt("id"));
            }
        }
        return a;
    }

    public List<String> insertarLote(List<Alumno> alumnos) throws SQLException {
        List<String> duplicados = new ArrayList<>();
        String sql = "INSERT INTO alumno (matricula) VALUES (?) ON CONFLICT (matricula) DO NOTHING";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Alumno a : alumnos) {
                    ps.setString(1, a.getMatricula());
                    int filas = ps.executeUpdate();
                    if (filas == 0) duplicados.add(a.getMatricula());
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

    public void crear(Alumno a, String passwordHash) throws SQLException {
        String sqlUsuario = "INSERT INTO usuario (nombre, email, password_hash, rol, activo) VALUES (?, ?, ?, 'alumno', true) RETURNING id";
        String sqlAlumno = "INSERT INTO alumno (usuario_id, matricula) VALUES (?, ?)";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                int nuevoUsuarioId = -1;

                try (PreparedStatement psUsuario = conn.prepareStatement(sqlUsuario)) {
                    psUsuario.setString(1, a.getNombre());
                    psUsuario.setString(2, a.getEmail());
                    psUsuario.setString(3, passwordHash);
                    try (ResultSet rs = psUsuario.executeQuery()) {
                        if (rs.next()) nuevoUsuarioId = rs.getInt(1);
                    }
                }

                try (PreparedStatement psAlumno = conn.prepareStatement(sqlAlumno)) {
                    psAlumno.setInt(1, nuevoUsuarioId);
                    psAlumno.setString(2, a.getMatricula());
                    psAlumno.executeUpdate();
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

    public void actualizar(Alumno a) throws SQLException {
        String sqlUsuario = "UPDATE usuario SET nombre = ?, email = ? WHERE id = ?";
        String sqlAlumno = "UPDATE alumno SET matricula = ? WHERE id = ?";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false); 
            try {
                try (PreparedStatement psUsuario = conn.prepareStatement(sqlUsuario)) {
                    psUsuario.setString(1, a.getNombre());
                    psUsuario.setString(2, a.getEmail());
                    psUsuario.setInt(3, a.getUsuarioId());
                    psUsuario.executeUpdate();
                }

                try (PreparedStatement psAlumno = conn.prepareStatement(sqlAlumno)) {
                    psAlumno.setString(1, a.getMatricula());
                    psAlumno.setInt(2, a.getId());
                    psAlumno.executeUpdate();
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

    public void cambiarEstado(int alumnoId, boolean estado) throws SQLException {
        String sql = "UPDATE usuario SET activo = ? WHERE id = (SELECT usuario_id FROM alumno WHERE id = ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, estado);
            ps.setInt(2, alumnoId);
            ps.executeUpdate();
        }
    }

    public void actualizarPassword(int alumnoId, String passwordHash) throws SQLException {
        String sql = "UPDATE usuario SET password_hash = ? WHERE id = (SELECT usuario_id FROM alumno WHERE id = ?)";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, alumnoId);
            ps.executeUpdate();
        }
    }

    public void eliminar(int idAlumno) throws SQLException {
        String sqlGetUsuario = "SELECT usuario_id FROM alumno WHERE id = ?";
        String sqlDelAlumno = "DELETE FROM alumno WHERE id = ?";
        String sqlDelUsuario = "DELETE FROM usuario WHERE id = ?";

        try (Connection conn = DatabaseManagerUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Integer usuarioId = null;
                try (PreparedStatement ps = conn.prepareStatement(sqlGetUsuario)) {
                    ps.setInt(1, idAlumno);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            int uid = rs.getInt("usuario_id");
                            if (!rs.wasNull()) usuarioId = uid;
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(sqlDelAlumno)) {
                    ps.setInt(1, idAlumno);
                    ps.executeUpdate();
                }

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