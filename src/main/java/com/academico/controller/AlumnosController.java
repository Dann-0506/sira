package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.CargaDatosService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;

public class AlumnosController {
    
    @FXML private TableView<Alumno> tablaAlumnos;
    @FXML private TableColumn<Alumno, String> colMatricula;
    @FXML private TableColumn<Alumno, String> colNombre;
    @FXML private TableColumn<Alumno, String> colEmail;
    @FXML private TableColumn<Alumno, Void> colAcciones;
    @FXML private Pagination paginacionAlumnos;
    @FXML private TextField campoBusqueda;
    @FXML private Label errorMatricula;
    @FXML private Label errorNombre;
    @FXML private Label errorEmail;

    @FXML private StackPane panelFormulario;
    @FXML private Label labelTituloFormulario;
    @FXML private TextField campoMatricula;
    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private Label mensajeGeneral;
    @FXML private Button botonGuardar;
    @FXML private Label labelNotaPassword;
    @FXML private Button btnRestablecerPassword;

    private AlumnoService alumnoService = new AlumnoService();
    private CargaDatosService cargaDatosService = new CargaDatosService();
    private ObservableList<Alumno> listaAlumnos = FXCollections.observableArrayList();
    private FilteredList<Alumno> alumnosFiltrados;
    private Alumno alumnoEnEdicion = null;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaAlumnos.setFixedCellSize(48);
        alumnosFiltrados = new FilteredList<>(listaAlumnos, p -> true);

        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button(); // Dinámico (Activar/Desactivar)
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEstado, btnEliminar);

            {
                btnEditar.getStyleClass().addAll("accent", "flat");
                btnEstado.getStyleClass().addAll("flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnEditar.setOnAction(e -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminacion(getTableView().getItems().get(getIndex())));
                btnEstado.setOnAction(e -> confirmarCambioEstado(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Alumno a = (Alumno) getTableRow().getItem();
                    
                    btnEstado.getStyleClass().removeAll("success", "warning");

                    if (a.isActivo()) {
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
            List<Alumno> alumnos = alumnoService.listarTodos();
            listaAlumnos.setAll(alumnos);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    private void configurarPaginacion() {
        int totalFilas = alumnosFiltrados.size();
        int totalPaginas = (int) Math.ceil((double) totalFilas / FILAS_POR_PAGINA);

        paginacionAlumnos.setPageCount(totalPaginas > 0 ? totalPaginas : 1);
        
        paginacionAlumnos.setPageFactory(pageIndex -> {
            int desde = pageIndex * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, totalFilas);
            
            if (desde < totalFilas) {
                tablaAlumnos.setItems(FXCollections.observableArrayList(alumnosFiltrados.subList(desde, hasta)));
            } else {
                tablaAlumnos.setItems(FXCollections.observableArrayList());
            }
            return new Region(); 
        });
    }

    @FXML
    private void handleGuardar() {
        // 1. Recolectar datos de la UI
        Alumno temporal = (alumnoEnEdicion != null) ? alumnoEnEdicion : new Alumno();
        temporal.setMatricula(campoMatricula.getText().trim());
        temporal.setNombre(campoNombre.getText().trim());
        temporal.setEmail(campoEmail.getText().trim());

        try {
            // 2. Llamar al servicio
            alumnoService.guardar(temporal, alumnoEnEdicion != null);
            
            // 3. Notificar éxito
            mostrarNotificacion("Operación realizada con éxito", false);
            cargarDatos();

            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e -> handleCancelar());
            pause.play();
        } catch (Exception e) {
            // 4. Notificar error traducido
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    private void abrirEdicion(Alumno a) {
        alumnoEnEdicion = a;
        
        campoMatricula.setText(a.getMatricula());
        campoNombre.setText(a.getNombre());
        campoEmail.setText(a.getEmail());
        
        labelTituloFormulario.setText("Editar Alumno");
        labelNotaPassword.setText("Nota: Si el usuario olvidó su acceso, puedes restablecer su contraseña.");
        btnRestablecerPassword.setVisible(true);
        btnRestablecerPassword.setManaged(true);

        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void confirmarEliminacion(Alumno a) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia Crítica");
        alert.setHeaderText("Vas a eliminar permanentemente a " + a.getNombre());
        alert.setContentText("Esta acción es irreversible. ¿Deseas continuar?");
        
        ButtonType btnEliminar = new ButtonType("Eliminar definitivamente", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnEliminar, btnCancelar);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnEliminar) {
            try {
                alumnoService.eliminar(a.getId());
                cargarDatos();
                mostrarNotificacion("Alumno eliminado permanentemente.", false);
            } catch (Exception e) {
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }

    private void confirmarCambioEstado(Alumno a) {
        boolean nuevoEstado = !a.isActivo();
        String accionText = nuevoEstado ? "Activar" : "Desactivar";
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar " + accionText);
        alert.setHeaderText("¿Deseas " + accionText.toLowerCase() + " el acceso de " + a.getNombre() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Asume que agregaste este método a AlumnoService como acordamos en el paso anterior
                alumnoService.cambiarEstado(a.getId(), nuevoEstado); 
                cargarDatos(); 
                mostrarNotificacion("Cuenta " + (nuevoEstado ? "activada" : "desactivada") + " correctamente.", false);
            } catch (Exception e) {
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }

    @FXML 
    private void handleNuevo() { 
        alumnoEnEdicion = null; 
        limpiarFormulario();
        
        labelTituloFormulario.setText("Nuevo Alumno");
        labelNotaPassword.setText("Nota: Los alumnos nuevos se crean con la contraseña predeterminada '123456'.");
        btnRestablecerPassword.setVisible(false);
        btnRestablecerPassword.setManaged(false);

        panelFormulario.setVisible(true); 
        panelFormulario.setManaged(true); 
    }

    @FXML private void handleCancelar() { 
        panelFormulario.setVisible(false);
        panelFormulario.setManaged(false);
        limpiarFormulario();
    }

    private void limpiarFormulario() {
        campoMatricula.clear();
        campoNombre.clear();
        campoEmail.clear();
    }

    private void mostrarNotificacion(String mensaje, boolean esError) {
        mensajeGeneral.setText(mensaje);
        mensajeGeneral.setOpacity(1.0); // Reset de opacidad obligatorio
        mensajeGeneral.setVisible(true);
        mensajeGeneral.setManaged(true);

        // Colores según el éxito o error (usando estilos de tu proyecto)
        if (esError) {
            mensajeGeneral.setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
        } else {
            mensajeGeneral.setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
        }

        // Animación: Se muestra y luego se desvanece
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.seconds(1), mensajeGeneral);
        fade.setDelay(javafx.util.Duration.seconds(2)); // Visible por 2 segundos
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            mensajeGeneral.setVisible(false);
            mensajeGeneral.setManaged(false);
        });
        fade.play();
    }

    @FXML 
    private void handleBusqueda() {
        String textoFiltro = campoBusqueda.getText().toLowerCase().trim();

        alumnosFiltrados.setPredicate(alumno -> {
            if (textoFiltro.isEmpty()) return true;

            return alumno.getNombre().toLowerCase().contains(textoFiltro) ||
                   alumno.getMatricula().toLowerCase().contains(textoFiltro);
        });

        configurarPaginacion();
    }


    @FXML
    private void handleImportarCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de Alumnos (CSV)");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos CSV", "*.csv")
        );

        // Abrir el selector de archivos
        File archivo = fileChooser.showOpenDialog(tablaAlumnos.getScene().getWindow());

        if (archivo != null) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                // Llamamos al orquestador
                List<String> errores = cargaDatosService.importarAlumnosCsv(fis);

                if (errores.isEmpty()) {
                    mostrarNotificacion("¡Todos los alumnos importados con éxito!", false);
                } else {
                    // Si hubo errores parciales, mostramos un resumen y los detalles
                    int fallidos = errores.size();
                    mostrarNotificacion("Importación completada con " + fallidos + " errores.", true);
                    
                    // Opcional: Mostrar detalles en un Alert para que el usuario sepa qué filas fallaron
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Detalle de errores en CSV");
                    alert.setHeaderText("Algunas filas no pudieron procesarse:");
                    alert.setContentText(String.join("\n", errores));
                    alert.showAndWait();
                }

                // Refrescamos la tabla para ver los nuevos datos
                cargarDatos(); 

            } catch (Exception e) {
                mostrarNotificacion("Error crítico al procesar el archivo.", true);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRestablecerPassword() {
        if (alumnoEnEdicion == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restablecer Contraseña");
        alert.setHeaderText("¿Deseas restablecer la contraseña de " + alumnoEnEdicion.getNombre() + "?");
        alert.setContentText("Su contraseña volverá a ser '123456' temporalmente.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                alumnoService.restablecerPassword(alumnoEnEdicion.getId());
                mostrarNotificacion("Contraseña restablecida a '123456'.", false);
                
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> handleCancelar());
                pause.play();
            } catch (Exception e) {
                mostrarNotificacion(e.getMessage(), true);
            }
        }
    }
}