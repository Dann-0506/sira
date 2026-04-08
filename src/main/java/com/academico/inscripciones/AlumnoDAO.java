package com.academico.inscripciones;

import com.academico.core.db.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlumnoDAO {

    // === Mapeo de ResultSet a Alumno ===

    private Alumno mapear(ResultSet rs) throws SQLException {
        Alumno a = new Alumno();
        a.setId(rs.getInt("id"));
        int uid = rs.getInt("usuario_id");
        a.setUsuarioId(rs.wasNull() ? null : uid);
        a.setMatricula(rs.getString("matricula"));
        a.setNombre(rs.getString("nombre"));
        a.setEmail(rs.getString("email"));
        return a;
    }


    // === Consulta ===

    public Optional<Alumno> findById(int id) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE a.id = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Alumno> findByMatricula(String matricula) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE a.matricula = ?
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, matricula);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Alumno> findAll() throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email
                FROM alumno a
                LEFT JOIN usuario u ON u.id = a.usuario_id
                ORDER BY u.nombre
                """;
        List<Alumno> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Alumno> findByGrupo(int grupoId) throws SQLException {
        String sql = """
                SELECT a.*, u.nombre, u.email
                FROM alumno a
                JOIN inscripcion i ON i.alumno_id = a.id
                LEFT JOIN usuario u ON u.id = a.usuario_id
                WHERE i.grupo_id = ?
                ORDER BY u.nombre
                """;
        List<Alumno> lista = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }


    // === Escritura ===

    public Alumno insertar(Alumno a) throws SQLException {
        String sql = """
                INSERT INTO alumno (usuario_id, matricula)
                VALUES (?, ?)
                RETURNING id
                """;
        try (Connection conn = DatabaseManager.getConnection();
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

        try (Connection conn = DatabaseManager.getConnection()) {
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


    // === Actualización ===

    public void actualizar(Alumno a) throws SQLException {
        String sql = "UPDATE alumno SET matricula = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, a.getMatricula());
            ps.setInt(2, a.getId());
            ps.executeUpdate();
        }
    }
}