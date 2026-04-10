package com.academico.controller;

import com.academico.model.Maestro;
import com.academico.service.CargaDatosService;
import com.academico.service.individuals.MaestroService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class MaestrosController { 

    // === ELEMENTOS PRINCIPALES ===
    @FXML private TableView<Maestro> tablaMaestros;
    @FXML private TableColumn<Maestro, String> colNumEmpleado;
    @FXML private TableColumn<Maestro, String> colNombre;
    @FXML private TableColumn<Maestro, String> colEmail;
    @FXML private TableColumn<Maestro, Void> colAcciones;
    @FXML private Pagination paginacionMaestros;
    @FXML private TextField campoBusqueda;

    // === FORMULARIO DOCENTE ===
    @FXML private StackPane panelFormulario;
    @FXML private Label labelTituloFormulario;
    @FXML private TextField campoNumEmpleado;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private Label labelNotaPassword;
    @FXML private Button btnRestablecerPassword;
    
    // Etiquetas de error reservadas para validaciones visuales
    @FXML private Label errorMatricula; 
    @FXML private Label errorNombre;
    @FXML private Label errorEmail;

    // === GLOBALES (Confirmación y Notificación) ===
    @FXML private StackPane panelConfirmacion;
    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblMensajeConfirmacion;
    @FXML private Button btnConfirmarAccion;
    @FXML private Label mensajeGeneral;

    // === VARIABLES DE ESTADO Y SERVICIOS ===
    private final MaestroService maestroService = new MaestroService();
    private final CargaDatosService cargaDatosService = new CargaDatosService();
    private final ObservableList<Maestro> listaMaestros = FXCollections.observableArrayList();
    private FilteredList<Maestro> maestrosFiltrados;
    private Maestro maestroEnEdicion = null;
    private Runnable accionPendiente;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaMaestros.setFixedCellSize(48);
        configurarColumnas();
        cargarDatos();
    }

    // ==========================================
    // LÓGICA PRINCIPAL DE DOCENTES
    // ==========================================

    private void configurarColumnas() {
        colNumEmpleado.setCellValueFactory(new PropertyValueFactory<>("numEmpleado"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button(); 
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEstado, btnEliminar); 
            
            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEstado.getStyleClass().addAll("flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> confirmarCambioEstado(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Maestro m = getTableView().getItems().get(getIndex());
                    
                    btnEstado.getStyleClass().removeAll("success", "warning");

                    if (m.isActivo()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.getStyleClass().add("warning");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.getStyleClass().add("success");
                    }
                    
                    setGraphic(panel);
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            List<Maestro> maestros = maestroService.listarTodos();
            listaMaestros.setAll(maestros);
            maestrosFiltrados = new FilteredList<>(listaMaestros, p -> true);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBusqueda() {
        String filtro = campoBusqueda.getText().toLowerCase().trim();
        maestrosFiltrados.setPredicate(m -> {
            if (filtro.isEmpty()) return true;
            return m.getNombre().toLowerCase().contains(filtro) || 
                   m.getNumEmpleado().toLowerCase().contains(filtro);
        });
        configurarPaginacion();
    }

    private void configurarPaginacion() {
        int total = maestrosFiltrados.size();
        int paginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        paginacionMaestros.setPageCount(paginas > 0 ? paginas : 1);
        paginacionMaestros.setPageFactory(idx -> {
            int desde = idx * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
            tablaMaestros.setItems(FXCollections.observableArrayList(maestrosFiltrados.subList(desde, hasta)));
            return new Region();
        });
    }

    // ==========================================
    // GESTIÓN DEL FORMULARIO
    // ==========================================

    @FXML 
    private void handleNuevo() { 
        maestroEnEdicion = null; 
        limpiarFormulario();
        
        labelTituloFormulario.setText("Nuevo Docente");
        labelNotaPassword.setText("Nota: Los docentes nuevos se crean con la contraseña predeterminada '123456'.");
        btnRestablecerPassword.setVisible(false);
        btnRestablecerPassword.setManaged(false);

        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    }

    private void abrirEdicion(Maestro m) {
        maestroEnEdicion = m;
        
        campoNumEmpleado.setText(m.getNumEmpleado());
        campoNombre.setText(m.getNombre());
        campoEmail.setText(m.getEmail());
        
        labelTituloFormulario.setText("Editar Docente");
        labelNotaPassword.setText("Nota: Si el usuario olvidó su acceso, puedes restablecer su contraseña.");
        btnRestablecerPassword.setVisible(true);
        btnRestablecerPassword.setManaged(true);

        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void handleGuardar() {
        Maestro m = (maestroEnEdicion != null) ? maestroEnEdicion : new Maestro();
        m.setNumEmpleado(campoNumEmpleado.getText().trim());
        m.setNombre(campoNombre.getText().trim());
        m.setEmail(campoEmail.getText().trim());

        try {
            maestroService.guardar(m, maestroEnEdicion != null);
            mostrarNotificacion("Docente guardado con éxito.", false);
            handleCancelar();
            cargarDatos();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleRestablecerPassword() {
        if (maestroEnEdicion == null) return;

        mostrarConfirmacion(
            "Restablecer Contraseña",
            "¿Deseas restablecer la contraseña de " + maestroEnEdicion.getNombre() + "?\nSu contraseña volverá a ser '123456' temporalmente.",
            "Restablecer",
            "danger",
            () -> {
                try {
                    maestroService.restablecerPassword(maestroEnEdicion.getId());
                    mostrarNotificacion("Contraseña restablecida a '123456'.", false);
                    handleCancelar();
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    @FXML 
    private void handleCancelar() { 
        panelFormulario.setVisible(false); 
        panelFormulario.setManaged(false); 
        limpiarFormulario();
    }

    private void limpiarFormulario() { 
        campoNumEmpleado.clear(); 
        campoNombre.clear(); 
        campoEmail.clear(); 
    }

    // ==========================================
    // COMPONENTES GLOBALES (Importación, Notificaciones y Modales)
    // ==========================================

    @FXML
    private void handleImportarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de Docentes (CSV)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));

        File archivo = fileChooser.showOpenDialog(tablaMaestros.getScene().getWindow());

        if (archivo != null) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                List<String> errores = cargaDatosService.importarMaestrosCsv(fis);

                if (errores.isEmpty()) {
                    mostrarNotificacion("¡Todos los docentes importados con éxito!", false);
                } else {
                    mostrarNotificacion("Importación completada con " + errores.size() + " errores.", true);
                    mostrarDetallesErrores(errores, tablaMaestros.getScene().getWindow()); 
                }
                cargarDatos(); 
                
            } catch (Exception e) {
                mostrarNotificacion("Error crítico al procesar el archivo.", true);
            }
        }
    }

    private void mostrarDetallesErrores(List<String> errores, javafx.stage.Window ventanaPadre) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(ventanaPadre); 
        alert.setTitle("Detalle de la Importación");
        alert.setHeaderText("Algunas filas no pudieron procesarse:");

        TextArea textArea = new TextArea(String.join("\n", errores));
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setPrefWidth(550);
        textArea.setPrefHeight(250);

        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }

    private void confirmarCambioEstado(Maestro m) {
        boolean nuevoEstado = !m.isActivo();
        String accionText = nuevoEstado ? "Activar" : "Desactivar";
        
        mostrarConfirmacion(
            "Confirmar " + accionText,
            "¿Deseas " + accionText.toLowerCase() + " el acceso del docente " + m.getNombre() + "?",
            accionText,
            nuevoEstado ? "accent" : "danger",
            () -> {
                try {
                    maestroService.cambiarEstado(m.getId(), nuevoEstado);
                    cargarDatos(); 
                    mostrarNotificacion("Cuenta del docente " + (nuevoEstado ? "activada" : "desactivada") + ".", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void confirmarEliminacion(Maestro m) {
        mostrarConfirmacion(
            "Advertencia Crítica",
            "Vas a eliminar permanentemente al docente " + m.getNombre() + ".\nEsta acción borrará todo su registro. ¿Deseas continuar?",
            "Eliminar definitivamente",
            "danger",
            () -> {
                try {
                    maestroService.eliminar(m.getId());
                    cargarDatos();
                    mostrarNotificacion("Docente eliminado permanentemente.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    private void mostrarNotificacion(String mensaje, boolean esError) {
        mensajeGeneral.setText(mensaje);
        mensajeGeneral.setOpacity(1.0);
        mensajeGeneral.setVisible(true);
        mensajeGeneral.setManaged(true);

        if (esError) {
            mensajeGeneral.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        } else {
            mensajeGeneral.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        }

        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(2));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
            mensajeGeneral.setManaged(false);
        });
        fade.play();
    }

    private void mostrarConfirmacion(String titulo, String mensaje, String textoBoton, String claseCSSBoton, Runnable accion) {
        lblTituloConfirmacion.setText(titulo);
        lblMensajeConfirmacion.setText(mensaje);
        btnConfirmarAccion.setText(textoBoton);

        btnConfirmarAccion.getStyleClass().removeAll("accent", "danger");
        btnConfirmarAccion.getStyleClass().add(claseCSSBoton);

        this.accionPendiente = accion;

        panelConfirmacion.setVisible(true);
        panelConfirmacion.setManaged(true);
    }

    @FXML
    private void handleCancelarConfirmacion() {
        panelConfirmacion.setVisible(false);
        panelConfirmacion.setManaged(false);
        accionPendiente = null;
    }

    @FXML
    private void handleEjecutarConfirmacion() {
        if (accionPendiente != null) {
            accionPendiente.run(); 
        }
        handleCancelarConfirmacion();
    }
}