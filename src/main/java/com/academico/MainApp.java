package com.academico;

import com.academico.util.DatabaseManagerUtil;
import com.academico.util.NavegationUtil;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Punto de entrada principal de la aplicación.
 * Responsabilidad: Gestionar el ciclo de vida de JavaFX, inicializar el Pool de 
 * conexiones y establecer el tema visual global.
 */
public class MainApp extends Application {

    private static Stage primaryStage;

    // ==========================================
    // CICLO DE VIDA DE LA APLICACIÓN
    // ==========================================

    @Override
    public void init() {
        // Se ejecuta antes de mostrar la interfaz (Hilo secundario)
        try {
            DatabaseManagerUtil.initialize();
        } catch (Exception e) {
            // Si la base de datos falla, la aplicación no puede funcionar
            System.err.println("FATAL: Fallo al inicializar infraestructura: " + e.getMessage());
            Platform.exit(); 
        }
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // 1. Configuración Estética Global
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // 2. Configuración de la Ventana Inicial (Login)
        stage.setTitle("Registro Académico — Gestión Escolar");
        stage.setResizable(false);
        stage.setMinWidth(500);
        stage.setMinHeight(650);
        stage.setWidth(500);
        stage.setHeight(650);

        // 3. Lanzamiento de la primera vista
        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.show();
        stage.centerOnScreen();
    }

    @Override
    public void stop() {
        // Se ejecuta al cerrar la aplicación para liberar recursos
        DatabaseManagerUtil.close();
    }

    // ==========================================
    // ACCESO GLOBAL AL ESCENARIO (Stage)
    // ==========================================

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}