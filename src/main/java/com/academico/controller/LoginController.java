package com.academico.controller;

import com.academico.model.Usuario;
import com.academico.service.AuthService;
import com.academico.util.NavegationUtil;
import com.academico.util.SessionManagerUtil;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Optional;

public class LoginController {

    // === ELEMENTOS PRINCIPALES ===
    @FXML private TextField campEmail;
    @FXML private PasswordField campPassword;
    @FXML private Button botonLogin;

    // === ETIQUETAS DE ERROR ===
    @FXML private Label errorEmail;
    @FXML private Label errorPassword;
    @FXML private Label errorGeneral;

    // === VARIABLES DE ESTADO Y SERVICIOS ===
    private final AuthService authService = new AuthService();

    // ==========================================
    // LÓGICA DE AUTENTICACIÓN
    // ==========================================

    @FXML
    private void handleLogin() {
        limpiarErrores();

        String email = campEmail.getText().trim();
        String password = campPassword.getText();
        boolean hayError = false;

        // Validación visual preliminar
        if (email.isEmpty()) {
            mostrarError(errorEmail, "El correo es obligatorio.");
            hayError = true;
        }
        if (password.isEmpty()) {
            mostrarError(errorPassword, "La contraseña es obligatoria.");
            hayError = true;
        }

        if (hayError) return;

        // Bloqueo de UI durante la consulta
        botonLogin.setDisable(true);

        try {
            Optional<Usuario> resultado = authService.login(email, password);

            if (resultado.isPresent()) {
                Usuario usuario = resultado.get();

                // Detección de contraseña insegura obligatoria
                if ("123456".equals(campPassword.getText())) {
                    usuario.setRequiereCambioPassword(true);
                }
                
                SessionManagerUtil.iniciarSesion(usuario);

                // Preparación de la ventana principal
                javafx.stage.Stage stage = (javafx.stage.Stage) botonLogin.getScene().getWindow();
                stage.setResizable(true);

                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                    stage.setMinWidth(1024);
                    stage.setMinHeight(720);
                    navegarSegunRol(usuario.getRol());
                });

            } else {
                mostrarError(errorGeneral, "Correo o contraseña incorrectos.");
            }

        } catch (Exception e) {
            mostrarError(errorGeneral, "Error de conexión.");
            System.err.println("Error en login: " + e.getMessage());
        } finally {
            botonLogin.setDisable(false);
        }
    }

    private void navegarSegunRol(String rol) {
        switch (rol) {
            case "admin", "maestro" -> NavegationUtil.irA(NavegationUtil.DASHBOARD);
            default -> mostrarError(errorGeneral, "Rol no reconocido. Contacta al administrador.");
        }
    }

    // ==========================================
    // GESTIÓN DE COMPONENTES UI
    // ==========================================

    private void mostrarError(Label label, String mensaje) {
        label.setText(mensaje);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void limpiarErrores() {
        Label[] labelsDeError = {errorEmail, errorPassword, errorGeneral};
        
        for (Label label : labelsDeError) {
            label.setVisible(false);
            label.setManaged(false);
            label.setText("");
        }
    }
}