package com.academico.controller;

import com.academico.model.Usuario;
import com.academico.service.individuals.UsuarioService;
import com.academico.util.NavegationUtil;
import com.academico.util.SessionManagerUtil;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {

    // === ELEMENTOS PRINCIPALES (Menú y Cabecera) ===
    @FXML private Label labelNombreUsuario;
    @FXML private Label labelRolUsuario;
    @FXML private Label labelBienvenida;
    @FXML private VBox menuNavegacion;
    @FXML private StackPane areaPrincipal;

    // === ELEMENTOS DE MI PERFIL (Panel Flotante) ===
    @FXML private StackPane panelPerfilFlotante;
    @FXML private TextField campoPerfilNombre;
    @FXML private TextField campoPerfilEmail;
    @FXML private PasswordField campoPerfilPassword;
    @FXML private Label mensajePerfil;

    // === VARIABLES DE ESTADO Y SERVICIOS ===
    private final UsuarioService usuarioService = new UsuarioService();
    private Button botonActivo;

    // ==========================================
    // INICIALIZACIÓN
    // ==========================================

    @FXML
    public void initialize() {
        Usuario usuario = SessionManagerUtil.getUsuarioActual();
        if (usuario == null) {
            NavegationUtil.irA(NavegationUtil.LOGIN);
            return;
        }

        labelNombreUsuario.setText(usuario.getNombre());
        labelRolUsuario.setText(formatearRol(usuario.getRol()));
        labelBienvenida.setText("Bienvenido, " + usuario.getNombre().split(" ")[0] + ".");

        construirMenu(usuario.getRol());

        // Verificación de seguridad por contraseña genérica
        if (usuario.isRequiereCambioPassword()) {
            javafx.application.Platform.runLater(() -> {
                abrirPerfilFlotante();
                mostrarNotificacionPerfil("¡Atención! Estás usando la contraseña predeterminada. Por tu seguridad, cámbiala ahora.", true, true);
            });
        }
    }

    // ==========================================
    // LÓGICA DE NAVEGACIÓN Y MENÚ
    // ==========================================

    private void construirMenu(String rol) {
        menuNavegacion.getChildren().clear();

        if ("admin".equals(rol)) {
            agregarSeccion("CATÁLOGOS");
            agregarBoton("Administradores", NavegationUtil.ADMINS);
            agregarBoton("Alumnos",         NavegationUtil.ALUMNOS);
            agregarBoton("Materias",        NavegationUtil.MATERIAS);
            agregarBoton("Maestros",        NavegationUtil.MAESTROS);
            agregarBoton("Grupos",          NavegationUtil.GRUPOS);
            agregarBoton("Inscripciones",   NavegationUtil.INSCRIPCIONES);

            agregarSeccion("SISTEMA");
            agregarBoton("Configuración", NavegationUtil.CONFIGURACION);
            agregarBoton("Utilerías",     NavegationUtil.UTILERIA);
        }

        if ("maestro".equals(rol)) {
            agregarSeccion("MIS GRUPOS");
            agregarBoton("Actividades",    NavegationUtil.ACTIVIDADES);
            agregarBoton("Calificaciones", NavegationUtil.CALIFICACIONES);
            agregarBoton("Reportes",       NavegationUtil.REPORTES);
        }

        agregarSeccion("CUENTA");
        Button btnPerfil = new Button("Mi perfil");
        btnPerfil.getStyleClass().add("flat");
        btnPerfil.setMaxWidth(Double.MAX_VALUE);
        btnPerfil.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btnPerfil.setOnAction(e -> abrirPerfilFlotante()); 
        menuNavegacion.getChildren().add(btnPerfil);
    }

    private void agregarSeccion(String titulo) {
        Label label = new Label(titulo);
        label.getStyleClass().add("sidebar-titulo");
        menuNavegacion.getChildren().add(label);
    }

    private void agregarBoton(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("flat");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        boton.setOnAction(e -> {
            actualizarBotonActivo(boton);
            NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
        });
        menuNavegacion.getChildren().add(boton);
    }

    private void actualizarBotonActivo(Button boton) {
        if (botonActivo != null) {
            botonActivo.getStyleClass().remove("accent");
            botonActivo.getStyleClass().add("flat");
        }
        botonActivo = boton;
        botonActivo.getStyleClass().remove("flat");
        botonActivo.getStyleClass().add("accent");
    }

    private String formatearRol(String rol) {
        return switch (rol) {
            case "admin"   -> "Administrador";
            case "maestro" -> "Docente";
            case "alumno"  -> "Estudiante";
            default        -> rol;
        };
    }

    // ==========================================
    // LÓGICA DE SESIÓN
    // ==========================================

    @FXML
    private void handleCerrarSesion() {
        SessionManagerUtil.cerrarSesion();

        javafx.stage.Stage stage = (javafx.stage.Stage) menuNavegacion.getScene().getWindow();

        // Restaurar dimensiones de la ventana de Login
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.setMinWidth(500);
        stage.setMinHeight(650);
        stage.setWidth(500);
        stage.setHeight(650);

        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.centerOnScreen();
    }

    // ==========================================
    // GESTIÓN DE MI PERFIL
    // ==========================================

    private void abrirPerfilFlotante() {
        Usuario actual = SessionManagerUtil.getUsuarioActual();
        campoPerfilNombre.setText(actual.getNombre());
        campoPerfilEmail.setText(actual.getEmail());
        campoPerfilPassword.clear();
        
        panelPerfilFlotante.setVisible(true);
        panelPerfilFlotante.setManaged(true);
    }

    @FXML
    private void handleGuardarPerfil() {
        Usuario actual = SessionManagerUtil.getUsuarioActual();
        String nuevoNombre = campoPerfilNombre.getText().trim();
        String nuevoEmail = campoPerfilEmail.getText().trim();
        String nuevaPass = campoPerfilPassword.getText();

        if (nuevoNombre.isEmpty() || nuevoEmail.isEmpty()) {
            mostrarNotificacionPerfil("El nombre y correo no pueden estar vacíos.", true, false); 
            return;
        }

        try {
            usuarioService.actualizarPerfil(actual.getId(), nuevoNombre, nuevoEmail, nuevaPass);
            
            // Actualizar variables y UI en caliente
            actual.setNombre(nuevoNombre);
            actual.setEmail(nuevoEmail);
            labelNombreUsuario.setText(nuevoNombre);
            labelBienvenida.setText("Bienvenido, " + nuevoNombre.split(" ")[0] + ".");
            
            mostrarNotificacionPerfil("Perfil actualizado con éxito.", false, false);
            
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleCerrarPerfil());
            pause.play();

        } catch (Exception e) {
            mostrarNotificacionPerfil(e.getMessage(), true, false);
        }
    }

    @FXML
    private void handleCerrarPerfil() {
        panelPerfilFlotante.setVisible(false);
        panelPerfilFlotante.setManaged(false);
    }

    private void mostrarNotificacionPerfil(String mensaje, boolean esError, boolean persistente) {
        mensajePerfil.setText(mensaje);
        mensajePerfil.setOpacity(1.0); 
        mensajePerfil.setVisible(true);
        mensajePerfil.setManaged(true);

        if (esError) {
            mensajePerfil.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 8; -fx-background-radius: 5;");
        } else {
            mensajePerfil.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 8; -fx-background-radius: 5;");
        }

        if (!persistente) {
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajePerfil);
            fade.setDelay(Duration.seconds(2));
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                mensajePerfil.setVisible(false);
                mensajePerfil.setManaged(false);
            });
            fade.play();
        }
    }
}