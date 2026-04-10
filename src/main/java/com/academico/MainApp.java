package com.academico;

import com.academico.util.DatabaseManagerUtil;
import com.academico.util.NavegationUtil;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        
        primaryStage = stage;

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        stage.setTitle("Sistema de Registro de Resultados Académicos");
        stage.setResizable(false);
        stage.setMinWidth(500);
        stage.setMinHeight(650);
        stage.setWidth(500);
        stage.setHeight(650);

        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.show();

        stage.centerOnScreen();
    }

    @Override
    public void init() {
        try {
            DatabaseManagerUtil.initialize();
        } catch (Exception e) {
            System.err.println("Error crítico al inicializar la base de datos: " + e.getMessage());
            javafx.application.Platform.exit();
        }
    }

    @Override
    public void stop() {
        DatabaseManagerUtil.close();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}