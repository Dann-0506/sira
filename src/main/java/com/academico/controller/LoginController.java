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

    @FXML private TextField     campEmail;
    @FXML private PasswordField campPassword;
    @FXML private Label         errorEmail;
    @FXML private Label         errorPassword;
    @FXML private Label         errorGeneral;
    @FXML private Button        botonLogin;

    @FXML
    private void handleLogin() {
        // Limpia errores previos
        limpiarErrores();

        String email    = campEmail.getText().trim();
        String password = campPassword.getText();

        // Validación local antes de tocar la BD
        boolean hayError = false;

        if (email.isEmpty()) {
            mostrarError(errorEmail, "El correo es obligatorio.");
            hayError = true;
        }

        if (password.isEmpty()) {
            mostrarError(errorPassword, "La contraseña es obligatoria.");
            hayError = true;
        }

        if (hayError) return;

        // Deshabilita el botón durante el proceso
        botonLogin.setDisable(true);

        try {
            AuthService authService = new AuthService();
            Optional<Usuario> resultado = authService.login(email, password);

            if (resultado.isPresent()) {
                Usuario usuario = resultado.get();
                SessionManagerUtil.iniciarSesion(usuario);

                javafx.stage.Stage stage = (javafx.stage.Stage) botonLogin.getScene().getWindow();
                stage.setResizable(true);

                

                javafx.application.Platform.runLater(() -> {
                    stage.setMaximized(true);
                    stage.setMinWidth(1024);
                    stage.setMinHeight(720);
                    navegarSegunRol(usuario.getRol());
                });
            } else {
                mostrarError(errorGeneral,
                    "Correo o contraseña incorrectos.");
            }

        } catch (Exception e) {
            mostrarError(errorGeneral,
                "Error de conexión.");
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

    private void mostrarError(Label label, String mensaje) {
        label.setText(mensaje);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void limpiarErrores() {
        for (Label label : new Label[]{errorEmail, errorPassword, errorGeneral}) {
            label.setVisible(false);
            label.setManaged(false);
            label.setText("");
        }
    }
}