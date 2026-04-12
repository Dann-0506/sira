package com.academico.controller;

import com.academico.model.Grupo;
import com.academico.model.Maestro;
import com.academico.model.Usuario;
import com.academico.service.individuals.MaestroService;
import com.academico.service.individuals.UsuarioService;
import com.academico.util.NavegationUtil;
import com.academico.util.SessionManagerUtil;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Controlador principal para el Dashboard del rol Maestro.
 * Gestiona el menú de navegación general, el menú contextual por grupo 
 * y la actualización del perfil del docente.
 */
public class DashboardMaestroController {

    // Singleton de UI para permitir comunicación desde otras vistas
    public static DashboardMaestroController instancia;

    // === ELEMENTOS PRINCIPALES (Menú y Cabecera) ===
    @FXML private Label labelNombreUsuario;
    @FXML private Label labelNumEmpleado;
    @FXML private VBox menuPrincipal;
    @FXML private VBox menuGrupoContextual;
    @FXML private StackPane areaPrincipal;

    // === ELEMENTOS DE MI PERFIL (Panel Flotante) ===
    @FXML private StackPane panelPerfilFlotante;
    @FXML private TextField campoPerfilNombre;
    @FXML private TextField campoPerfilEmail;
    @FXML private PasswordField campoPerfilPassword;
    @FXML private Label mensajePerfil;

    // === VARIABLES DE ESTADO Y SERVICIOS ===
    private final MaestroService maestroService = new MaestroService();
    private final UsuarioService usuarioService = new UsuarioService();
    
    private Maestro perfilMaestro;
    private Grupo grupoSeleccionado;
    private Button botonActivo;

    // ==========================================
    // INICIALIZACIÓN
    // ==========================================

    @FXML
    public void initialize() {
        instancia = this;
        Usuario usuario = SessionManagerUtil.getUsuarioActual();
        if (usuario == null) {
            NavegationUtil.irA(NavegationUtil.LOGIN);
            return;
        }

        try {
            perfilMaestro = maestroService.buscarPorUsuarioId(usuario.getId());
            labelNombreUsuario.setText(usuario.getNombre());
            labelNumEmpleado.setText("No. Empleado: " + perfilMaestro.getNumEmpleado());
            
            construirMenuPrincipal();
            
            abrirVista(NavegationUtil.MIS_GRUPOS, null);

            if (usuario.isRequiereCambioPassword()) {
                javafx.application.Platform.runLater(() -> {
                    abrirPerfilFlotante();
                    mostrarNotificacionPerfil("¡Atención! Estás usando la contraseña predeterminada. Por tu seguridad, cámbiala ahora.", true, true);
                });
            }

        } catch (Exception e) {
            System.err.println("Error crítico al cargar perfil docente: " + e.getMessage());
        }
    }

    // ==========================================
    // CONSTRUCCIÓN DE MENÚS (Navegación Dinámica)
    // ==========================================

    private void construirMenuPrincipal() {
        menuPrincipal.getChildren().clear();
        
        Label titulo = new Label("HOME");
        titulo.getStyleClass().add("sidebar-titulo");
        menuPrincipal.getChildren().add(titulo);

        Button btnMisGrupos = crearBotonMenu("Mis Grupos Asignados", null);
        
        btnMisGrupos.setOnAction(e -> {
            actualizarBotonActivo(btnMisGrupos);
            
            menuGrupoContextual.setVisible(false);
            menuGrupoContextual.setManaged(false);
            this.grupoSeleccionado = null;
            
            NavegationUtil.cargarEnArea(areaPrincipal, NavegationUtil.MIS_GRUPOS);
        });

        Button btnPerfil = crearBotonMenu("Mi Perfil", null); 
        btnPerfil.setOnAction(e -> abrirPerfilFlotante());
        
        menuPrincipal.getChildren().addAll(btnMisGrupos, btnPerfil);
    }

    /**
     * Activa el menú contextual cuando el maestro selecciona un grupo específico
     * desde la pantalla de "Mis Grupos".
     */
    public void activarMenuDeGrupo(Grupo grupo) {
        this.grupoSeleccionado = grupo;
        menuGrupoContextual.getChildren().clear();

        Label titulo = new Label("GESTIÓN DEL GRUPO");
        titulo.getStyleClass().add("sidebar-titulo");
        
        Label subtitulo = new Label(grupo.getClave() + " - " + grupo.getMateriaNombre());
        subtitulo.setStyle("-fx-font-size: 11px; -fx-text-fill: #0969da; -fx-padding: 0 0 10 10; -fx-font-weight: bold;");

        Button btnActividades = crearBotonMenu("Rúbrica y Actividades", NavegationUtil.GRUPO_ACTIVIDADES);
        Button btnCalificaciones = crearBotonMenu("Calificar Alumnos", NavegationUtil.GRUPO_CALIFICACIONES);
        Button btnBonus = crearBotonMenu("Asignar Puntos Extra", NavegationUtil.GRUPO_BONUS);
        Button btnConcentrado = crearBotonMenu("Concentrado Final", NavegationUtil.GRUPO_CONCENTRADO);

        menuGrupoContextual.getChildren().addAll(titulo, subtitulo, btnActividades, btnCalificaciones, btnBonus, btnConcentrado);
        
        menuGrupoContextual.setVisible(true);
        menuGrupoContextual.setManaged(true);
        
        // Al seleccionar un grupo, lo mandamos a la pantalla de actividades por defecto
        btnActividades.fire();
    }

    private Button crearBotonMenu(String texto, String rutaFxml) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("flat");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        if (rutaFxml != null) {
            boton.setOnAction(e -> {
                actualizarBotonActivo(boton);
                NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
            });
        }
        return boton;
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

    public void abrirVista(String rutaFxml, Button botonOrigen) {
        if (botonOrigen != null) actualizarBotonActivo(botonOrigen);
        NavegationUtil.cargarEnArea(areaPrincipal, rutaFxml);
    }

    // ==========================================
    // GESTIÓN DE PERFIL
    // ==========================================

    @FXML
    private void abrirPerfilFlotante() {
        Usuario u = SessionManagerUtil.getUsuarioActual();
        if (u != null) {
            campoPerfilNombre.setText(u.getNombre());
            campoPerfilEmail.setText(u.getEmail());
            campoPerfilPassword.clear();
            
            // Ocultar mensajes previos
            mensajePerfil.setVisible(false);
            mensajePerfil.setManaged(false);
            
            // Mostrar modal
            panelPerfilFlotante.setVisible(true);
            panelPerfilFlotante.setManaged(true);
        }
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
            
            actual.setNombre(nuevoNombre);
            actual.setEmail(nuevoEmail);
            labelNombreUsuario.setText(nuevoNombre);

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
        mensajePerfil.setVisible(true);
        mensajePerfil.setManaged(true);
        mensajePerfil.setOpacity(1.0);

        if (esError) {
            mensajePerfil.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 8; -fx-background-radius: 5;");
        } else {
            mensajePerfil.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 8; -fx-background-radius: 5;");
        }

        if (!persistente) {
            FadeTransition fade = new FadeTransition(Duration.seconds(1), mensajePerfil);
            fade.setDelay(Duration.seconds(2));
            fade.setToValue(0.0);
            fade.play();
        }
    }

    // ==========================================
    // SISTEMA
    // ==========================================

    @FXML
    private void handleCerrarSesion() {
        SessionManagerUtil.cerrarSesion();
        instancia = null;
        
        javafx.stage.Stage stage = (javafx.stage.Stage) menuPrincipal.getScene().getWindow();
        
        stage.setMaximized(false);
        stage.setResizable(false);
        stage.setMinWidth(500);
        stage.setMinHeight(650);
        stage.setWidth(500);
        stage.setHeight(650);

        NavegationUtil.irA(NavegationUtil.LOGIN);
        stage.centerOnScreen();
    }

    public Grupo getGrupoSeleccionado() {
        return this.grupoSeleccionado;
    }
}