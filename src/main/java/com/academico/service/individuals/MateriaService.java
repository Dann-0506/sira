package com.academico.service.individuals;

import com.academico.dao.MateriaDAO;
import com.academico.model.Materia;
import com.academico.dao.UnidadDAO;
import com.academico.model.Unidad;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la gestión de Materias.
 * Responsabilidad: Controlar la creación de materias y la auto-generación de su temario (Unidades).
 */
public class MateriaService {

    // === DEPENDENCIAS ===
    private final MateriaDAO materiaDAO;
    private final UnidadDAO unidadDAO;

    // === CONSTRUCTORES ===
    public MateriaService() {
        this.materiaDAO = new MateriaDAO();
        this.unidadDAO = new UnidadDAO();
    }

    public MateriaService(MateriaDAO materiaDAO, UnidadDAO unidadDAO) {
        this.materiaDAO = materiaDAO;
        this.unidadDAO = unidadDAO;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Materia> listarTodas() throws Exception {
        try { 
            return materiaDAO.findAll(); 
        } catch (SQLException e) { 
            throw new Exception("Error al cargar el catálogo de materias desde la base de datos."); 
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================

    public void guardar(Materia materia, boolean esEdicion) throws Exception {
        // 1. Validaciones tempranas
        if (materia.getClave() == null || materia.getClave().isBlank() || 
            materia.getNombre() == null || materia.getNombre().isBlank()) {
            throw new IllegalArgumentException("La clave y el nombre de la materia son obligatorios.");
        }
        if (materia.getTotalUnidades() <= 0) {
            throw new IllegalArgumentException("Una materia debe tener como mínimo 1 unidad.");
        }
        
        // 2. Persistencia y Autogeneración
        try {
            if (esEdicion) {
                materiaDAO.actualizar(materia);
            } else {
                // Guardamos la materia y obtenemos el objeto con su ID real (PostgreSQL RETURNING id)
                Materia mGuardada = materiaDAO.insertar(materia);
                
                // Generamos las N unidades individualmente
                for (int i = 1; i <= mGuardada.getTotalUnidades(); i++) {
                    Unidad u = new Unidad();
                    u.setMateriaId(mGuardada.getId());
                    u.setNumero(i);
                    u.setNombre("Unidad " + i);
                    unidadDAO.insertar(u);
                }
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new Exception("Error: La clave de materia ingresada ya existe.");
            }
            throw new Exception("Error de conexión al intentar guardar la materia.");
        }
    }
    
    // ==========================================
    // OPERACIONES DE ELIMINACIÓN
    // ==========================================

    public void eliminar(int id) throws Exception {
        try {
            materiaDAO.eliminar(id);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: Esta materia ya está siendo impartida en uno o más grupos.");
            }
            throw new Exception("Error al intentar eliminar la materia.");
        }
    }
}