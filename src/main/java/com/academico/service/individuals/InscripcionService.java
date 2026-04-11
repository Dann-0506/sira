package com.academico.service.individuals;

import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para las Inscripciones.
 * Responsabilidad: Gestionar el registro de alumnos en grupos y la aplicación de 
 * calificaciones manuales (overrides) por parte de los docentes.
 */
public class InscripcionService {

    // === DEPENDENCIAS ===
    private final InscripcionDAO inscripcionDAO;

    // === CONSTRUCTORES ===
    public InscripcionService() {
        this.inscripcionDAO = new InscripcionDAO();
    }

    public InscripcionService(InscripcionDAO inscripcionDAO) {
        this.inscripcionDAO = inscripcionDAO;
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA (Inscripción)
    // ==========================================

    /**
     * Registra un alumno en un grupo validando reglas de negocio y unicidad.
     */
    public void inscribir(Inscripcion inscripcion) throws Exception {
        if (inscripcion.getAlumnoId() <= 0 || inscripcion.getGrupoId() <= 0) {
            throw new IllegalArgumentException("El alumno y el grupo son obligatorios para procesar la inscripción.");
        }

        try {
            inscripcionDAO.insertar(inscripcion);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new Exception("Error: El alumno ya se encuentra inscrito en este grupo.");
            }
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("Error: El alumno o el grupo especificado no existe.");
            }
            throw new Exception("Error de conexión al procesar la inscripción en la base de datos.");
        }
    }

    // ==========================================
    // OPERACIONES DE LECTURA Y ESTADO
    // ==========================================

    public List<Inscripcion> listarPorGrupo(int grupoId) throws Exception {
        if (grupoId <= 0) throw new IllegalArgumentException("ID de grupo inválido.");

        try {
            return inscripcionDAO.findByGrupo(grupoId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar la lista de alumnos inscritos.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            inscripcionDAO.eliminar(id);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede dar de baja: El alumno ya tiene calificaciones o actividades registradas en este grupo.");
            }
            throw new Exception("Error al intentar dar de baja la inscripción.");
        }
    }

    // ==========================================
    // OPERACIONES ACADÉMICAS ESPECIALES
    // ==========================================

    /**
     * Aplica una calificación final directa ignorando los cálculos matemáticos (Override).
     */
    public void aplicarOverrideMateria(int inscripcionId, BigDecimal calificacionManual, String justificacion) throws Exception {
        // 1. Validaciones de negocio para la calificación manual
        if (calificacionManual != null) {
            if (calificacionManual.compareTo(BigDecimal.ZERO) < 0 || calificacionManual.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalArgumentException("La calificación manual debe estar entre 0 y 100.");
            }
            if (justificacion == null || justificacion.isBlank()) {
                throw new IllegalArgumentException("Se requiere escribir una justificación académica al aplicar una calificación manual.");
            }
        }

        // 2. Persistencia
        try {
            // Verificar que la inscripción exista antes de intentar actualizar
            inscripcionDAO.findById(inscripcionId)
                    .orElseThrow(() -> new Exception("La inscripción que intentas modificar ya no existe."));
            
            inscripcionDAO.actualizarOverride(inscripcionId, calificacionManual, justificacion);
            
        } catch (SQLException e) {
            throw new Exception("Error de conexión al guardar la calificación manual.");
        }
    }
}