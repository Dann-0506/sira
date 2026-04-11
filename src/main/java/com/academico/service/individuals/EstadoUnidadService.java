package com.academico.service.individuals;

import com.academico.dao.EstadoUnidadDAO;
import com.academico.model.EstadoUnidad;

import java.sql.SQLException;

/**
 * Servicio de Gestión de Estados Académicos.
 * Responsabilidad: Controlar si una unidad está ABIERTA (editable) o CERRADA (bloqueada) 
 * para un grupo específico.
 */
public class EstadoUnidadService {

    // === DEPENDENCIAS ===
    private final EstadoUnidadDAO estadoUnidadDAO;

    // === CONSTRUCTORES ===
    public EstadoUnidadService() {
        this.estadoUnidadDAO = new EstadoUnidadDAO();
    }

    public EstadoUnidadService(EstadoUnidadDAO estadoUnidadDAO) {
        this.estadoUnidadDAO = estadoUnidadDAO;
    }

    // ==========================================
    // VALIDACIONES DE NEGOCIO
    // ==========================================

    public void validarUnidadAbierta(int grupoId, int unidadId) throws Exception {
        try {
            EstadoUnidad estado = estadoUnidadDAO.findByGrupoYUnidad(grupoId, unidadId);
            
            // Si el estado existe y está marcado explícitamente como cerrado, bloqueamos.
            if (estado != null && estado.isCerrada()) {
                throw new IllegalStateException("Acción no permitida: La unidad ya ha sido cerrada.");
            }
        } catch (SQLException e) {
            throw new Exception("Error de conexión al verificar el estado de la unidad.");
        }
    }

    // ==========================================
    // OPERACIONES DE LECTURA Y ESCRITURA
    // ==========================================

    public EstadoUnidad obtenerEstado(int grupoId, int unidadId) throws Exception {
        try {
            return estadoUnidadDAO.findByGrupoYUnidad(grupoId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar el estado actual de la unidad.");
        }
    }

    public void cerrarUnidad(int grupoId, int unidadId) throws Exception {
        try {
            estadoUnidadDAO.guardarEstado(grupoId, unidadId, "CERRADA");
        } catch (SQLException e) {
            throw new Exception("Error al intentar cerrar la unidad en la base de datos.");
        }
    }

    public void abrirUnidad(int grupoId, int unidadId) throws Exception {
        try {
            estadoUnidadDAO.guardarEstado(grupoId, unidadId, "ABIERTA");
        } catch (SQLException e) {
            throw new Exception("Error al intentar reabrir la unidad en la base de datos.");
        }
    }
}