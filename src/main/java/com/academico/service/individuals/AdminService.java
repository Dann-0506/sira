package com.academico.service.individuals;

import com.academico.dao.AdminDAO;
import com.academico.model.Usuario;
import com.academico.service.AuthService;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio CRUD para la entidad Administrador.
 * Responsabilidad: Aplicar reglas de negocio y manejar la persistencia de usuarios administradores.
 */
public class AdminService {

    // === DEPENDENCIAS ===
    private final AdminDAO adminDAO;
    private final AuthService authService;

    // === CONSTRUCTORES ===
    public AdminService() {
        this.adminDAO = new AdminDAO();
        this.authService = new AuthService();
    }

    public AdminService(AdminDAO adminDAO, AuthService authService) {
        this.adminDAO = adminDAO;
        this.authService = authService;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public List<Usuario> listarAdmins() throws Exception {
        try {
            return adminDAO.findAllAdmins();
        } catch (SQLException e) {
            throw new Exception("Error al cargar la lista de administradores desde la base de datos.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA Y ACTUALIZACIÓN
    // ==========================================

    public void guardar(Usuario admin, boolean esEdicion) throws Exception {
        // 1. Validaciones tempranas de integridad
        if (admin.getNombre() == null || admin.getNombre().isBlank() || 
            admin.getEmail() == null || admin.getEmail().isBlank()) {
            throw new IllegalArgumentException("El nombre y el correo electrónico son obligatorios.");
        }
        
        if (!admin.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }

        // 2. Persistencia segura
        try {
            if (esEdicion) {
                adminDAO.actualizar(admin);
            } else {
                // Contraseña genérica por defecto al crear
                String hashSeguro = authService.hashearPassword("123456");
                adminDAO.crear(admin, hashSeguro);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState()) && e.getMessage() != null && e.getMessage().contains("email")) {
                throw new Exception("El correo electrónico ya está registrado en el sistema.");
            }
            throw new Exception("Error de conexión al intentar guardar el administrador.");
        }
    }

    public void restablecerPassword(int id) throws Exception {
        try {
            String hashSeguro = authService.hashearPassword("123456");
            adminDAO.actualizarPassword(id, hashSeguro);
        } catch (SQLException e) {
            throw new Exception("Error al restablecer la contraseña del administrador.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESTADO Y ELIMINACIÓN
    // ==========================================

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            adminDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al cambiar el estado de acceso del administrador.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            adminDAO.eliminar(id);
        } catch (SQLException e) {
            throw new Exception("Error al intentar eliminar permanentemente al administrador.");
        }
    }
}