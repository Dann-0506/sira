package com.academico.academia.dao;

import com.academico.academia.model.EstadoUnidad;
import com.academico.core.db.DatabaseManager;

import java.sql.*;

public class EstadoUnidadDAO {

    // === Mapeo de ResultSet a EstadoUnidad ===

    private EstadoUnidad mapear(ResultSet rs) throws SQLException {
        EstadoUnidad eu = new EstadoUnidad();
        eu.setId(rs.getInt("id"));
        eu.setGrupoId(rs.getInt("grupo_id"));
        eu.setUnidadId(rs.getInt("unidad_id"));
        eu.setEstado(rs.getString("estado"));
        eu.setActualizadoEn(rs.getTimestamp("actualizado_en").toLocalDateTime());
        return eu;
    }


    // === Consulta ===

    public EstadoUnidad findByGrupoYUnidad(int grupoId, int unidadId) throws SQLException {
        String sql = "SELECT * FROM estado_unidad WHERE grupo_id = ? AND unidad_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            ps.setInt(2, unidadId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        
        // Si no se encontró en la BD, devolvemos un objeto por defecto "ABIERTO"
        EstadoUnidad estadoPorDefecto = new EstadoUnidad();
        estadoPorDefecto.setGrupoId(grupoId);
        estadoPorDefecto.setUnidadId(unidadId);
        estadoPorDefecto.setEstado("ABIERTA");
        return estadoPorDefecto;
    }


    // === Actualización ===
    public void guardarEstado(int grupoId, int unidadId, String nuevoEstado) throws SQLException {
        String sql = """
                INSERT INTO estado_unidad (grupo_id, unidad_id, estado, actualizado_en)
                VALUES (?, ?, ?, NOW())
                ON CONFLICT (grupo_id, unidad_id) 
                DO UPDATE SET estado = EXCLUDED.estado, actualizado_en = NOW()
                """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, grupoId);
            ps.setInt(2, unidadId);
            ps.setString(3, nuevoEstado);
            ps.executeUpdate();
        }
    }
}