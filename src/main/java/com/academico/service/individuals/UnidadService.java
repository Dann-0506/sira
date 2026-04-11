package com.academico.service.individuals;

import com.academico.dao.UnidadDAO;
import com.academico.model.Unidad;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la gestión de Unidades (Temario).
 * Responsabilidad: Consultar y modificar la estructura temática de las materias y grupos.
 */
public class UnidadService {
    
    // === DEPENDENCIAS ===
    private final UnidadDAO unidadDAO;

    // === CONSTRUCTORES ===
    public UnidadService(){
        this.unidadDAO = new UnidadDAO();
    }

    public UnidadService(UnidadDAO unidadDAO) {
        this.unidadDAO = unidadDAO;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Unidad> listarPorGrupo(int grupoId) throws Exception {
        if (grupoId <= 0) throw new IllegalArgumentException("ID de grupo inválido.");
        try {
            return unidadDAO.findByGrupo(grupoId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar las unidades del grupo desde la base de datos.");
        }
    }

    public List<Unidad> listarPorMateria(int materiaId) throws Exception {
        if (materiaId <= 0) throw new IllegalArgumentException("ID de materia inválido.");
        try {
            return unidadDAO.findByMateria(materiaId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar el temario de la materia.");
        }
    }

    // ==========================================
    // OPERACIONES DE ACTUALIZACIÓN
    // ==========================================

    public void actualizarNombre(int id, String nombre) throws Exception {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la unidad no puede estar vacío.");
        }
        
        try {
            unidadDAO.actualizarNombre(id, nombre);
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar actualizar el nombre de la unidad.");
        }
    }
}