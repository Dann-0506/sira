package com.academico.service.individuals;

import com.academico.dao.GrupoDAO;
import com.academico.model.Grupo;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la gestión de Grupos.
 * Responsabilidad: Coordinar la creación y asignación de grupos a materias y maestros.
 */
public class GrupoService {
    
    // === DEPENDENCIAS ===
    private final GrupoDAO grupoDAO;

    // === CONSTRUCTORES ===
    public GrupoService() {
        this.grupoDAO = new GrupoDAO();
    }

    public GrupoService(GrupoDAO grupoDAO) {
        this.grupoDAO = grupoDAO;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Grupo> listarTodos() throws Exception {
        try { 
            return grupoDAO.findAll(); 
        } catch (SQLException e) { 
            throw new Exception("Error al cargar la lista de grupos desde la base de datos."); 
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================

    public void guardar(Grupo grupo) throws Exception {
        // 1. Validaciones tempranas de negocio
        if (grupo.getMateriaId() <= 0 || grupo.getMaestroId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una materia y un docente válidos.");
        }
        if (grupo.getClave() == null || grupo.getClave().isBlank()) {
            throw new IllegalArgumentException("La clave del grupo es obligatoria.");
        }
        if (grupo.getSemestre() == null || grupo.getSemestre().isBlank()) {
            throw new IllegalArgumentException("El semestre es obligatorio.");
        }

        // 2. Persistencia segura
        try {
            grupoDAO.insertar(grupo);
        } catch (SQLException e) {
            String state = e.getSQLState();
            if ("23505".equals(state)) {
                throw new Exception("Error: La clave de grupo ya existe en el sistema.");
            }
            if ("23503".equals(state)) {
                throw new Exception("Error: La materia o el docente seleccionados no existen o fueron eliminados.");
            }
            throw new Exception("Error de conexión al intentar crear el grupo.");
        }
    }
}