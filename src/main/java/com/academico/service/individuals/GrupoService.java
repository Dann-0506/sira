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

    public List<Grupo> buscarGruposPorMaestro(int maestroId) throws Exception {
        try {
            return grupoDAO.findByMaestro(maestroId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar los grupos del maestro desde la base de datos.");
        }
    }

    public Grupo buscarPorClave(String clave) throws Exception {
        try {
            return grupoDAO.findByClave(clave)
                .orElseThrow(() -> new Exception("El grupo con clave '" + clave + "' no existe."));
        } catch (SQLException e) {
            throw new Exception("Error de conexión al buscar el grupo por clave.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================

    public void guardar(Grupo grupo, boolean esEdicion) throws Exception {
        if (grupo.getMateriaId() <= 0 || grupo.getMaestroId() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una materia y un docente válidos.");
        }
        if (grupo.getClave() == null || grupo.getClave().isBlank()) {
            throw new IllegalArgumentException("La clave del grupo es obligatoria.");
        }
        if (grupo.getSemestre() == null || grupo.getSemestre().isBlank()) {
            throw new IllegalArgumentException("El semestre es obligatorio.");
        }

        try {
            if (esEdicion) {
                grupoDAO.actualizar(grupo);
            } else {
                grupoDAO.insertar(grupo);
            }
        } catch (SQLException e) {
            String state = e.getSQLState();
            if ("23505".equals(state)) {
                throw new Exception("Error: La clave de grupo ya existe en el sistema.");
            }
            if ("23503".equals(state)) {
                throw new Exception("Error: La materia o el docente seleccionados no existen.");
            }
            throw new Exception("Error de conexión al intentar guardar el grupo.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESTADO Y ELIMINACIÓN
    // ==========================================

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            // Obtenemos el grupo primero para actualizar solo el estado
            Grupo g = grupoDAO.findById(id).orElseThrow(() -> new Exception("Grupo no encontrado."));
            g.setActivo(estado);
            grupoDAO.actualizar(g);
        } catch (SQLException e) {
            throw new Exception("Error al actualizar el estado del grupo.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            grupoDAO.eliminar(id); 
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El grupo ya tiene alumnos inscritos.");
            }
            throw new Exception("Error al eliminar el grupo.");
        }
    }
}