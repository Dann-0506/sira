package com.academico.service.individuals;

import com.academico.dao.UsuarioDAO;
import com.academico.service.AuthService;
import java.sql.SQLException;

/**
 * Servicio para la gestión de Perfil de Usuario.
 * Responsabilidad: Proveer a los usuarios activos la capacidad de actualizar 
 * sus datos personales y credenciales de forma segura.
 */
public class UsuarioService {
    
    // === DEPENDENCIAS ===
    private final UsuarioDAO usuarioDAO;
    private final AuthService authService;

    // === CONSTRUCTORES ===
    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.authService = new AuthService();
    }

    public UsuarioService(UsuarioDAO usuarioDAO, AuthService authService) {
        this.usuarioDAO = usuarioDAO;
        this.authService = authService;
    }

    // ==========================================
    // OPERACIONES DE PERFIL
    // ==========================================

    public void actualizarPerfil(int id, String nombre, String email, String nuevaPassword) throws Exception {
        // 1. Validaciones de Negocio
        if (nombre == null || nombre.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("El nombre y el correo electrónico son obligatorios.");
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
        }

        try {
            // 2. Hash de contraseña (solo si el usuario escribió una nueva)
            String hash = null;
            if (nuevaPassword != null && !nuevaPassword.isBlank()) {
                hash = authService.hashearPassword(nuevaPassword);
            }

            // 3. Persistencia
            usuarioDAO.actualizarPerfil(id, nombre, email, hash);

        } catch (SQLException e) {
            // 4. Traducción de errores SQL
            if ("23505".equals(e.getSQLState()) && e.getMessage() != null && e.getMessage().contains("email")) {
                throw new Exception("Error: El correo ingresado ya está registrado por otro usuario.");
            }
            throw new Exception("Error de conexión al intentar actualizar el perfil.");
        }
    }
}