package com.academico.controller;

import com.academico.model.Maestro;
import com.academico.service.individuals.MaestroService;
import com.academico.service.CargaDatosService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;

public class MaestrosController { 

    @FXML private TableView<Maestro> tablaMaestros;
    @FXML private TableColumn<Maestro, String> colNumEmpleado;
    @FXML private TableColumn<Maestro, String> colNombre;
    @FXML private TableColumn<Maestro, String> colEmail;
    @FXML private TableColumn<Maestro, Void> colAcciones;
    @FXML private Pagination paginacionMaestros;
    @FXML private TextField campoBusqueda;
    @FXML private TextField campoNumEmpleado;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private VBox panelFormulario;
    @FXML private Label labelTituloFormulario;
    @FXML private Label mensajeGeneral;
    @FXML private Label errorEmail;
    @FXML private Label errorNombre;
    @FXML private Label errorMatricula;

    private final MaestroService maestroService = new MaestroService();
    private final CargaDatosService cargaDatosService = new CargaDatosService();
    private final ObservableList<Maestro> listaMaestros = FXCollections.observableArrayList();
    private FilteredList<Maestro> maestrosFiltrados;
    private Maestro maestroEnEdicion = null;
    private final int FILAS_POR_PAGINA = 14;

    @FXML
    public void initialize() {
        tablaMaestros.setFixedCellSize(50);
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colNumEmpleado.setCellValueFactory(new PropertyValueFactory<>("numEmpleado"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button(); 
            private final Button btnEliminar = new Button("Eliminar");
            // SOLUCIÓN: Agregamos los 3 botones al panel
            private final HBox panel = new HBox(8, btnEditar, btnEstado, btnEliminar); 
            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> confirmarCambioEstado(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Maestro m = getTableView().getItems().get(getIndex());
                    if (m.isActivo()) {
                        btnEstado.setText("Desactivar");
                        btnEstado.setStyle("-fx-text-fill: #856404; -fx-background-color: #fff3cd;");
                    } else {
                        btnEstado.setText("Activar");
                        btnEstado.setStyle("-fx-text-fill: #155724; -fx-background-color: #d4edda;");
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

    @FXML
    private void handleGuardar() {
        Maestro m = (maestroEnEdicion != null) ? maestroEnEdicion : new Maestro();
        m.setNumEmpleado(campoNumEmpleado.getText().trim());
        m.setNombre(campoNombre.getText().trim());
        m.setEmail(campoEmail.getText().trim());

        try {
            maestroService.guardar(m, maestroEnEdicion != null);
            mostrarNotificacion("Docente guardado con éxito", false);
            handleCancelar();
            cargarDatos();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleImportarCsv() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showOpenDialog(tablaMaestros.getScene().getWindow());
        if (file != null) {
            try (FileInputStream fis = new FileInputStream(file)) {
                List<String> errores = cargaDatosService.importarMaestrosCsv(fis);
                if (errores.isEmpty()) mostrarNotificacion("Importación exitosa", false);
                else mostrarNotificacion("Completado con " + errores.size() + " errores.", true);
                cargarDatos();
            } catch (Exception e) {
                mostrarNotificacion("Error al procesar archivo", true);
            }
        }
    }

    private void abrirEdicion(Maestro m) {
        maestroEnEdicion = m;
        campoNumEmpleado.setText(m.getNumEmpleado());
        campoNombre.setText(m.getNombre());
        campoEmail.setText(m.getEmail());
        labelTituloFormulario.setText("Editar Docente");
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void confirmarCambioEstado(Maestro m) {
        boolean nuevoEstado = !m.isActivo();
        String accionText = nuevoEstado ? "Activar" : "Desactivar";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar " + accionText);
        alert.setHeaderText("¿Deseas " + accionText.toLowerCase() + " el acceso del docente " + m.getNombre() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                maestroService.cambiarEstado(m.getId(), nuevoEstado);
                cargarDatos(); 
                mostrarNotificacion("Cuenta del docente " + (nuevoEstado ? "activada" : "desactivada") + ".", false);
            } catch (Exception e) {
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }

    private void confirmarEliminacion(Maestro m) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia Crítica");
        alert.setHeaderText("Vas a eliminar permanentemente al docente " + m.getNombre());
        alert.setContentText("Esta acción borrará todo su registro. ¿Deseas continuar?");
        
        ButtonType btnEliminar = new ButtonType("Eliminar definitivamente", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnEliminar, btnCancelar);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnEliminar) {
            try {
                maestroService.eliminar(m.getId());
                cargarDatos();
                mostrarNotificacion("Docente eliminado permanentemente.", false);
            } catch (Exception e) {
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }

    @FXML 
    private void handleNuevo() { 
        maestroEnEdicion = null; 
        limpiar(); 
        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    
    }
    @FXML 
    private void handleCancelar() { 
        panelFormulario.setVisible(false); 
        panelFormulario.setManaged(false); 
    }

    private void limpiar() { 
        campoNumEmpleado.clear(); 
        campoNombre.clear(); 
        campoEmail.clear(); 
    }

    private void mostrarNotificacion(String mensaje, boolean esError) {
        mensajeGeneral.setText(mensaje);
        mensajeGeneral.setOpacity(1.0); // Reset de opacidad obligatorio
        mensajeGeneral.setVisible(true);
        mensajeGeneral.setManaged(true);

        // Colores según el éxito o error
        if (esError) {
            mensajeGeneral.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        } else {
            mensajeGeneral.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
        }

        // Animación: Se muestra y luego se desvanece suavemente
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), mensajeGeneral);
        fade.setDelay(javafx.util.Duration.seconds(2)); // Se mantiene visible por 2 segundos
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
            mensajeGeneral.setManaged(false);
        });
        fade.play();
    }
}