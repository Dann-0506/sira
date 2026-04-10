package com.academico.service.individuals;

import com.academico.dao.UsuarioDAO;
import com.academico.service.AuthService;
import java.sql.SQLException;

public class UsuarioService {
    
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AuthService authService = new AuthService();

    public void actualizarPerfil(int id, String nombre, String email, String nuevaPassword) throws Exception {
        // 1. Validaciones de Negocio
        if (nombre == null || nombre.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("El nombre y el correo son obligatorios.");
        }

        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
            throw new IllegalArgumentException("El formato del correo es inválido.");
        }

        try {
            // 2. Hash de contraseña (solo si el usuario escribió una nueva)
            String hash = null;
            if (nuevaPassword != null && !nuevaPassword.isBlank()) {
                hash = authService.hashearPassword(nuevaPassword);
            }

            // 3. Llamada a la capa de datos
            usuarioDAO.actualizarPerfil(id, nombre, email, hash);

        } catch (SQLException e) {
            // 4. Manejo de errores SQL específicos
            if ("23505".equals(e.getSQLState()) && e.getMessage().contains("email")) {
                throw new Exception("El correo ya está registrado por otro usuario.");
            }
            throw new Exception("Error interno al actualizar el perfil.");
        }
    }
}
