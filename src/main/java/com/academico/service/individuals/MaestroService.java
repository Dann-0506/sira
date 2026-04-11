package com.academico.service.individuals;

import com.academico.dao.MaestroDAO;
import com.academico.model.Maestro;
import com.academico.service.AuthService;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la entidad Maestro (Docente).
 * Responsabilidad: Aplicar reglas de negocio, validar datos y coordinar la persistencia de profesores.
 */
public class MaestroService {
    
    // === DEPENDENCIAS ===
    private final MaestroDAO maestroDAO;
    private final AuthService authService;

    // === CONSTRUCTORES ===
    public MaestroService() {
        this.maestroDAO = new MaestroDAO();
        this.authService = new AuthService();
    }

    public MaestroService(MaestroDAO maestroDAO, AuthService authService) {
        this.maestroDAO = maestroDAO;
        this.authService = authService;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Maestro> listarTodos() throws Exception {
        try { 
            return maestroDAO.findAll(); 
        } catch (SQLException e) { 
            throw new Exception("Error al cargar la lista de docentes desde la base de datos."); 
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void guardar(Maestro maestro, boolean esEdicion) throws Exception {
        // 1. Validaciones tempranas
        if (maestro.getNombre() == null || maestro.getNombre().isBlank() || 
            maestro.getNumEmpleado() == null || maestro.getNumEmpleado().isBlank()) {
            throw new IllegalArgumentException("El nombre y el número de empleado son obligatorios.");
        }
        
        if (maestro.getEmail() != null && !maestro.getEmail().isBlank()) {
            if (!maestro.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
            }
        }

        // 2. Persistencia segura
        try {
            if (esEdicion) {
                maestroDAO.actualizar(maestro);
            } else {
                String hashSeguro = authService.hashearPassword("123456");
                maestroDAO.crear(maestro, hashSeguro);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage() != null && e.getMessage().contains("num_empleado")) {
                    throw new Exception("Error: El número de empleado ya está asignado a otro docente.");
                }
                if (e.getMessage() != null && e.getMessage().contains("email")) {
                    throw new Exception("Error: El correo electrónico ya está registrado en el sistema.");
                }
                throw new Exception("Error: Un dato único ya existe en el sistema.");
            }
            throw new Exception("Error de conexión al intentar registrar al docente.");
        }
    }

    public void restablecerPassword(int id) throws Exception {
        try {
            String hashSeguro = authService.hashearPassword("123456");
            maestroDAO.actualizarPassword(id, hashSeguro);
        } catch (SQLException e) {
            throw new Exception("Error al intentar restablecer la contraseña del docente.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESTADO Y ELIMINACIÓN
    // ==========================================

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            maestroDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al actualizar el estado de acceso del docente.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            maestroDAO.eliminar(id);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El docente tiene grupos asignados. Utiliza la opción 'Desactivar'.");
            }
            throw new Exception("Error al intentar eliminar permanentemente el registro del docente.");
        }
    }
}