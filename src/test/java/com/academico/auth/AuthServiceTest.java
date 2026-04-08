package com.academico.auth;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
    }

    @Test
    @DisplayName("El hash generado debe tener un formato BCrypt válido")
    void testHashearPassword_FormatoValido() {
        String passwordPlano = "miPasswordSecreto123";
        String hash = authService.hashearPassword(passwordPlano);

        assertNotNull(hash, "El hash no debería ser nulo");
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"), 
                   "El hash debe empezar con el identificador de BCrypt");
        assertEquals(60, hash.length(), "Un hash de BCrypt siempre debe tener 60 caracteres");
    }

    @Test
    @DisplayName("El mismo password generado dos veces debe dar hashes diferentes (Salt aleatoria)")
    void testHashearPassword_SalAleatoria() {
        String passwordPlano = "123456";
        
        String hash1 = authService.hashearPassword(passwordPlano);
        String hash2 = authService.hashearPassword(passwordPlano);

        assertNotEquals(hash1, hash2, "Los hashes deben ser diferentes por la sal aleatoria");
    }

    @Test
    @DisplayName("Un hash generado por el servicio debe poder ser verificado correctamente")
    void testVerificacionExitosa() {
        String passwordPlano = "admin123";
        String hashGenerado = authService.hashearPassword(passwordPlano);

        // Verificamos usando la misma librería que usa el método login() internamente
        BCrypt.Result resultadoCorrecto = BCrypt.verifyer().verify(passwordPlano.toCharArray(), hashGenerado);
        BCrypt.Result resultadoIncorrecto = BCrypt.verifyer().verify("passwordEquivocado".toCharArray(), hashGenerado);

        assertTrue(resultadoCorrecto.verified, "La contraseña correcta debería ser verificada exitosamente");
        assertFalse(resultadoIncorrecto.verified, "Una contraseña incorrecta no debería ser verificada");
    }
}