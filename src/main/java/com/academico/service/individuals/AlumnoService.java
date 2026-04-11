package com.academico.service.individuals;

import com.academico.dao.AlumnoDAO;
import com.academico.model.Alumno;
import com.academico.service.AuthService;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la entidad Alumno.
 * Responsabilidad: Aplicar reglas de negocio, validar datos y coordinar la persistencia de estudiantes.
 */
public class AlumnoService {

    // === DEPENDENCIAS ===
    private final AlumnoDAO alumnoDAO;
    private final AuthService authService;

    // === CONSTRUCTORES ===
    public AlumnoService() {
        this.alumnoDAO = new AlumnoDAO();
        this.authService = new AuthService();
    }

    public AlumnoService(AlumnoDAO alumnoDAO, AuthService authService) {
        this.alumnoDAO = alumnoDAO;
        this.authService = authService;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Alumno> listarTodos() throws Exception {
        try {
            return alumnoDAO.obtenerTodos();
        } catch (SQLException e) {
            throw new Exception("Error al cargar la lista de alumnos desde la base de datos.");
        }
    }

    public Alumno buscarPorId(int id) throws Exception {
        try {
            return alumnoDAO.findById(id).orElse(new Alumno());
        } catch (SQLException e) {
            throw new Exception("Error al buscar los detalles del alumno.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void guardar(Alumno alumno, boolean esEdicion) throws Exception {
        // 1. Validaciones tempranas de negocio
        if (alumno.getMatricula() == null || alumno.getMatricula().isBlank() || 
            alumno.getNombre() == null || alumno.getNombre().isBlank()) {
            throw new IllegalArgumentException("La matrícula y el nombre son campos obligatorios.");
        }

        // Validación opcional de correo (si el usuario decidió ingresar uno)
        if (alumno.getEmail() != null && !alumno.getEmail().isBlank() && 
            !alumno.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }

        // 2. Persistencia segura
        try {
            if (esEdicion) {
                alumnoDAO.actualizar(alumno);
            } else {
                // Bug Corregido: La contraseña genérica ahora es correctamente "123456"
                String hashSeguro = authService.hashearPassword("123456");
                alumnoDAO.crear(alumno, hashSeguro);
            }
        } catch (SQLException e) {
            // TRADUCCIÓN AMIGABLE DE ERRORES POSTGRESQL
            String state = e.getSQLState();
            if ("23505".equals(state)) { // Violación de campo único (Unique)
                if (e.getMessage() != null && e.getMessage().contains("matricula")) {
                    throw new Exception("Error: La matrícula ingresada ya está asignada a otro alumno.");
                }
                if (e.getMessage() != null && e.getMessage().contains("email")) {
                    throw new Exception("Error: El correo electrónico ya está registrado en el sistema.");
                }
                throw new Exception("Error: Un dato único ya existe en el sistema.");
            }
            throw new Exception("Error de conexión al intentar guardar el alumno.");
        }
    }

    public void restablecerPassword(int id) throws Exception {
        try {
            String hashSeguro = authService.hashearPassword("123456");
            alumnoDAO.actualizarPassword(id, hashSeguro);
        } catch (SQLException e) {
            throw new Exception("Error al restablecer la contraseña del alumno.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESTADO Y ELIMINACIÓN
    // ==========================================

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            alumnoDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al actualizar el estado de acceso del alumno.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            alumnoDAO.eliminar(id);
        } catch (SQLException e) {
            // Manejo de Integridad Referencial
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El alumno tiene registros académicos vinculados. Utiliza la opción 'Desactivar'.");
            }
            throw new Exception("Error al intentar eliminar permanentemente el registro del alumno.");
        }
    }
}