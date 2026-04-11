package com.academico.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.academico.dao.UsuarioDAO;
import com.academico.model.Usuario;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Servicio de Autenticación y Seguridad.
 * Responsabilidad: Gestionar el acceso de usuarios, verificación de credenciales,
 * control de cuentas activas y el cifrado/descifrado seguro de contraseñas.
 */
public class AuthService {

    // === CONSTANTES DE SEGURIDAD ===
    private static final int BCRYPT_COST = 12;

    // === DEPENDENCIAS ===
    private final UsuarioDAO usuarioDAO;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public AuthService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    // ==========================================
    // LÓGICA DE AUTENTICACIÓN
    // ==========================================

    public Optional<Usuario> login(String email, String passwordPlano) throws Exception {
        if (email == null || email.isBlank() || passwordPlano == null || passwordPlano.isBlank()) {
            return Optional.empty();
        }

        try {
            // 1. Buscar usuario por correo electrónico
            Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                // 2. Validar que la cuenta no haya sido suspendida
                if (!usuario.isActivo()) {
                    throw new Exception("Tu cuenta ha sido desactivada. Contacta al administrador.");
                }

                // 3. Verificar la contraseña plana contra el Hash seguro de la BD
                BCrypt.Result resultado = BCrypt.verifyer().verify(passwordPlano.toCharArray(), usuario.getPasswordHash());
                
                if (resultado.verified) {
                    return Optional.of(usuario); // Login exitoso
                }
            }
            
            // Credenciales incorrectas o correo inexistente
            return Optional.empty(); 

        } catch (SQLException e) {
            // Propagamos el error real hacia el controlador para no enmascarar caídas de la Base de Datos
            throw new Exception("Error de conexión con la base de datos durante el inicio de sesión.");
        }
    }

    // ==========================================
    // UTILERÍAS DE CRIPTOGRAFÍA
    // ==========================================

    public String hashearPassword(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía antes de encriptarse.");
        }
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, passwordPlano.toCharArray());
    }
}