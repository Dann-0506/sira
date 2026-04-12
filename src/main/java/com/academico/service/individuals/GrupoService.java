package com.academico.service.individuals;

import com.academico.dao.GrupoDAO;
import com.academico.dao.InscripcionDAO;
import com.academico.model.CalificacionFinal;
import com.academico.model.Grupo;
import com.academico.model.Inscripcion;
import com.academico.service.ReporteService;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la gestión de Grupos.
 * Responsabilidad: Coordinar la creación y asignación de grupos a materias y maestros.
 */
public class GrupoService {
    
    // === DEPENDENCIAS ===
    private final GrupoDAO grupoDAO;
    private final ConfiguracionService configuracionService;

    // === CONSTRUCTORES ===
    public GrupoService() {
        this.grupoDAO = new GrupoDAO();
        this.configuracionService = new ConfiguracionService();
    }

    public GrupoService(GrupoDAO grupoDAO, ConfiguracionService configuracionService) {
        this.grupoDAO = grupoDAO;
        this.configuracionService = configuracionService;
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

    public Grupo buscarPorId(int id) throws Exception {
        try {
            return grupoDAO.findById(id)
                    .orElseThrow(() -> new Exception("No se encontró un grupo con el ID especificado."));
        } catch (SQLException e) {
            throw new Exception("Error de base de datos al buscar el grupo por ID.", e);
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

    public Grupo buscarPorClaveYSemestre(String clave, String semestre) throws Exception {
        try {
            return grupoDAO.findByClaveYSemestre(clave, semestre)
                .orElseThrow(() -> new Exception("El grupo '" + clave + "' para el semestre '" + semestre + "' no existe."));
        } catch (SQLException e) {
            throw new Exception("Error de conexión al buscar el grupo.");
        }
    }

    public List<Grupo> buscarGruposPorAlumno(int alumnoId) throws Exception {
        if (alumnoId <= 0) {
            throw new IllegalArgumentException("ID de alumno inválido.");
        }
        try {
            return grupoDAO.findByAlumno(alumnoId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar los cursos inscritos del alumno.");
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
                grupo.setCalificacionMinimaAprobatoria(configuracionService.obtenerCalificacionMinima());
                grupo.setCalificacionMaxima(configuracionService.obtenerCalificacionMaxima());
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

    public void cerrarCurso(int id) throws Exception {
    try {
        // 1. Snapshot de promedios finales (Persistencia histórica)
        congelarCalificacionesFinales(id);
        
        // 2. Cambio de estado en la BD
        grupoDAO.actualizarEstadoEvaluacion(id, "CERRADO");
        
    } catch (SQLException e) {
        throw new Exception("Error al cerrar el acta del grupo.");
    }
}

    public void reabrirCurso(int id) throws Exception {
        try {
            // 1. Abre evaluación y REACTIVA el grupo
            grupoDAO.actualizarEstadoActa(id, "ABIERTO", true);
            
            // 2. ¡CRÍTICO! Limpiamos el Snapshot para que el sistema vuelva a calcular "on-demand"
            InscripcionDAO inscripcionDAO = new InscripcionDAO();
            InscripcionService inscripcionService = new InscripcionService();
            
            List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(id);
            for (Inscripcion i : inscripciones) {
                // Devolvemos las celdas a NULL
                inscripcionDAO.guardarResultadosHistoricos(i.getId(), null, "PENDIENTE");
            }
            
        } catch (Exception e) {
            throw new Exception("Error al intentar reabrir el curso: " + e.getMessage());
        }
    }

    public void cerrarCursoDefinitivamente(int id) throws Exception {
        try {
            // Cierra evaluación y DESACTIVA el grupo
            grupoDAO.actualizarEstadoActa(id, "CERRADO", false);
        } catch (SQLException e) {
            throw new Exception("Error al intentar finalizar el curso.");
        }
    }

    private void congelarCalificacionesFinales(int grupoId) throws Exception {
        ReporteService reporteService = new ReporteService();
        InscripcionDAO inscripcionDAO = new InscripcionDAO();
        
        // Obtenemos los límites específicos de este grupo
        Grupo grupo = grupoDAO.findById(grupoId).orElseThrow(() -> new Exception("Grupo no encontrado."));
        List<CalificacionFinal> reporte = reporteService.generarReporteFinalGrupo(grupoId, grupo.getCalificacionMaxima());

        for (CalificacionFinal cf : reporte) {
            // Guardamos físicamente el resultado en la tabla inscripcion
            inscripcionDAO.guardarResultadosHistoricos(
                cf.getInscripcionId(), 
                cf.getCalificacionFinal(), 
                cf.getCalificacionFinal().compareTo(grupo.getCalificacionMinimaAprobatoria()) >= 0 ? "APROBADO" : "REPROBADO"
            );
        }
    }

    public void eliminar(int id) throws Exception {
        // Validación de regla de negocio
        Grupo grupo = grupoDAO.findById(id).orElseThrow(() -> new Exception("Grupo no encontrado."));
        if (grupo.isCerrado()) {
            throw new Exception("Operación denegada: No se puede eliminar un grupo histórico que ya tiene un acta cerrada.");
        }

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