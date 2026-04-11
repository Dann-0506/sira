package com.academico.dao;

import java.sql.*;
import java.util.Optional;

import com.academico.util.DatabaseManagerUtil;

/**
 * Objeto de Acceso a Datos (DAO) para la Configuración del Sistema.
 * Maneja un esquema simple de clave-valor para ajustes globales.
 */
public class ConfiguracionDAO {

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<String> findValor(String clave) throws SQLException {
        String sql = "SELECT valor FROM configuracion WHERE clave = ?";
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString("valor")) : Optional.empty();
            }
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void actualizar(String clave, String valor) throws SQLException {
        // Se utiliza UPSERT (Insert or Update) nativo de PostgreSQL
        String sql = """
                INSERT INTO configuracion (clave, valor)
                VALUES (?, ?)
                ON CONFLICT (clave) DO UPDATE SET valor = EXCLUDED.valor
                """;
        try (Connection conn = DatabaseManagerUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, clave);
            ps.setString(2, valor);
            ps.executeUpdate();
        }
    }
}