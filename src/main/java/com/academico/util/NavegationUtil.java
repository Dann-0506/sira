package com.academico.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.academico.MainApp;

public class NavegationUtil {

    public static final String LOGIN          = "/com/academico/ui/login.fxml";
    public static final String DASHBOARD      = "/com/academico/ui/dashboard.fxml";
    public static final String ALUMNOS        = "/com/academico/ui/alumnos.fxml";
    public static final String MATERIAS       = "/com/academico/ui/materias.fxml";
    public static final String MAESTROS       = "/com/academico/ui/maestros.fxml";
    public static final String GRUPOS         = "/com/academico/ui/grupos.fxml";
    public static final String INSCRIPCIONES  = "/com/academico/ui/inscripciones.fxml";
    public static final String CONFIGURACION  = "/com/academico/ui/configuracion.fxml";
    public static final String UTILERIA       = "/com/academico/ui/utileria.fxml";
    public static final String ACTIVIDADES    = "/com/academico/ui/actividades.fxml";
    public static final String CALIFICACIONES = "/com/academico/ui/calificaciones.fxml";
    public static final String REPORTES       = "/com/academico/ui/reportes.fxml";
    public static final String PERFIL         = "/com/academico/ui/perfil.fxml";

    private static final Map<String, String> TITULOS = new HashMap<>();

    static {
        TITULOS.put(LOGIN,          "Iniciar Sesión");
        TITULOS.put(DASHBOARD,      "Panel Principal");
        TITULOS.put(ALUMNOS,        "Alumnos");
        TITULOS.put(MATERIAS,       "Materias");
        TITULOS.put(MAESTROS,       "Maestros");
        TITULOS.put(GRUPOS,         "Grupos");
        TITULOS.put(INSCRIPCIONES,  "Inscripciones");
        TITULOS.put(CONFIGURACION,  "Configuración");
        TITULOS.put(UTILERIA,       "Utilerías");
        TITULOS.put(ACTIVIDADES,    "Actividades");
        TITULOS.put(CALIFICACIONES, "Calificaciones");
        TITULOS.put(REPORTES,       "Reportes");
        TITULOS.put(PERFIL,         "Mi Perfil");
    }

    /**
     * Reemplaza la escena completa de la ventana principal.
     * Usado para login ↔ dashboard.
     */
    public static void irA(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                NavegationUtil.class.getResource(ruta));
            Scene scene = new Scene(loader.load());

            String titulo = TITULOS.getOrDefault(ruta, "Registro Académico");
            MainApp.getPrimaryStage().setTitle("Registro Académico — " + titulo);
            MainApp.getPrimaryStage().setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + ruta, e);
        }
    }

    /**
     * Carga una vista dentro del área principal del dashboard.
     */
    public static void cargarEnArea(StackPane area, String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                NavegationUtil.class.getResource(ruta));
            Node vista = loader.load();
            area.getChildren().setAll(vista);
        } catch (IOException e) {
            Label placeholder = new Label("Vista en construcción");
            placeholder.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 14px;");
            area.getChildren().setAll(placeholder);
        }
    }

    /**
     * Versión que devuelve el FXMLLoader para acceder al controlador
     * de la vista cargada y pasarle datos.
     */
    public static FXMLLoader cargarEnAreaConLoader(StackPane area, String ruta) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            NavegationUtil.class.getResource(ruta));
        Node vista = loader.load();
        area.getChildren().setAll(vista);
        return loader;
    }
}