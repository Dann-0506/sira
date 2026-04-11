package com.academico.util;

import com.academico.MainApp;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Utilería para la Navegación y Control de Vistas (JavaFX).
 * Responsabilidad: Centralizar las rutas FXML, inyectar vistas en el contenedor
 * principal y gestionar los títulos de las ventanas.
 */
public class NavegationUtil {
    
    // === RUTAS DEL SISTEMA ===
    public static final String LOGIN             = "/com/academico/ui/login.fxml";
    public static final String DASHBOARD_ADMIN   = "/com/academico/ui/dashboard_admin.fxml";
    public static final String DASHBOARD_MAESTRO = "/com/academico/ui/dashboard_maestro.fxml"; // NUEVA
    
    // Rutas Admin
    public static final String ADMINS         = "/com/academico/ui/admins.fxml";
    public static final String ALUMNOS        = "/com/academico/ui/alumnos.fxml";
    public static final String MATERIAS       = "/com/academico/ui/materias.fxml";
    public static final String MAESTROS       = "/com/academico/ui/maestros.fxml";
    public static final String GRUPOS         = "/com/academico/ui/grupos.fxml";
    public static final String INSCRIPCIONES  = "/com/academico/ui/inscripciones.fxml";
    public static final String CONFIGURACION  = "/com/academico/ui/configuracion.fxml";
    public static final String RESPALDOS      = "/com/academico/ui/respaldos.fxml";
    
    // Rutas Maestro
    public static final String MIS_GRUPOS           = "/com/academico/ui/mis_grupos.fxml";
    public static final String GRUPO_ACTIVIDADES    = "/com/academico/ui/grupo_actividades.fxml";
    public static final String GRUPO_CALIFICACIONES = "/com/academico/ui/grupo_calificaciones.fxml";
    public static final String GRUPO_BONUS          = "/com/academico/ui/grupo_bonus.fxml";
    public static final String GRUPO_CONCENTRADO    = "/com/academico/ui/grupo_concentrado.fxml";
    

    private static final Map<String, String> TITULOS = new HashMap<>();

    static {
        TITULOS.put(LOGIN,          "Iniciar Sesión");
        TITULOS.put(DASHBOARD_ADMIN,      "Panel de Administración");
        TITULOS.put(DASHBOARD_MAESTRO, "Portal Docente");

        TITULOS.put(ADMINS,         "Administradores");
        TITULOS.put(ALUMNOS,        "Alumnos");
        TITULOS.put(MATERIAS,       "Materias");
        TITULOS.put(MAESTROS,       "Maestros");
        TITULOS.put(GRUPOS,         "Grupos");
        TITULOS.put(INSCRIPCIONES,  "Inscripciones");
        TITULOS.put(CONFIGURACION,  "Configuración");
        TITULOS.put(RESPALDOS,      "Respaldos");

        TITULOS.put(MIS_GRUPOS,           "Mis Grupos");
        TITULOS.put(GRUPO_ACTIVIDADES,    "Rúbrica de Evaluación");
        TITULOS.put(GRUPO_CALIFICACIONES, "Calificaciones por Unidad");
        TITULOS.put(GRUPO_BONUS,          "Asignaciar Puntos Extra");
        TITULOS.put(GRUPO_CONCENTRADO,    "Concentrado Final");
    }

    // ==========================================
    // MÉTODOS DE NAVEGACIÓN
    // ==========================================

    public static void irA(String ruta) {
        try {
            FXMLLoader loader = new FXMLLoader(NavegationUtil.class.getResource(ruta));
            Scene scene = new Scene(loader.load());

            String titulo = TITULOS.getOrDefault(ruta, "Registro Académico");
            MainApp.getPrimaryStage().setTitle("Registro Académico — " + titulo);
            MainApp.getPrimaryStage().setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista principal: " + ruta, e);
        }
    }

    public static void cargarEnArea(StackPane area, String ruta) {
        try {
            URL url = NavegationUtil.class.getResource(ruta);
            if (url == null) throw new Exception("No se encontró el archivo FXML.");

            FXMLLoader loader = new FXMLLoader(url);
            Node vista = loader.load();
            area.getChildren().setAll(vista);

        } catch (Exception e) {
            Label errorLabel = new Label("⚠ Error al cargar la vista seleccionada.\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #721c24; -fx-background-color: #f8d7da; -fx-padding: 20; -fx-background-radius: 10; -fx-font-weight: bold;");
            area.getChildren().setAll(errorLabel);
        }
    }

    public static FXMLLoader cargarEnAreaConLoader(StackPane area, String ruta) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavegationUtil.class.getResource(ruta));
        Node vista = loader.load();
        area.getChildren().setAll(vista);
        return loader;
    }
}