package com.academico.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.academico.dao.UsuarioDAO;
import com.academico.model.Usuario;

import java.util.Optional;

public class AuthService {

    private final UsuarioDAO usuarioDAO;

    public AuthService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public String hashearPassword(String passwordPlano) {
        return BCrypt.withDefaults().hashToString(12, passwordPlano.toCharArray());
    }

    public Optional<Usuario> login(String email, String passwordPlano) {
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(email);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                
                if (!usuario.isActivo()) {
                    return Optional.empty();
                }

                BCrypt.Result resultado = BCrypt.verifyer().verify(passwordPlano.toCharArray(), usuario.getPasswordHash());
                
                if (resultado.verified) {
                    return Optional.of(usuario); // Login exitoso
                }
            }
        } catch (Exception e) {
            System.err.println("Error durante el proceso de login: " + e.getMessage());
        }
        
        return Optional.empty(); // Fallo de autenticación
    }
}