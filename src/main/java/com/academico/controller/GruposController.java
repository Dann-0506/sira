package com.academico.controller;

import com.academico.model.Grupo;
import com.academico.model.Maestro;
import com.academico.model.Materia;
import com.academico.service.CargaDatosService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.MaestroService;
import com.academico.service.individuals.MateriaService;

import javafx.application.Platform;
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
import java.io.InputStream;
import java.util.List;

public class GruposController {

    // === ELEMENTOS DE LA INTERFAZ ===
    @FXML private TableView<Grupo> tablaGrupos;
    @FXML private TableColumn<Grupo, String> colClave, colMateria, colMaestro, colSemestre;
    @FXML private TableColumn<Grupo, Void> colAcciones;
    @FXML private TableColumn<Grupo, Boolean> colEstado;
    @FXML private Pagination paginacionGrupos;
    @FXML private TextField campoBusqueda;

    @FXML private StackPane panelFormulario, panelConfirmacion;
    @FXML private Label labelTituloFormulario, lblTituloConfirmacion, lblMensajeConfirmacion, mensajeGeneral;
    @FXML private ComboBox<Materia> cbMateria;
    @FXML private ComboBox<Maestro> cbMaestro;
    @FXML private TextField campoClave, campoSemestre;
    @FXML private Button btnConfirmarAccion;

    // === LISTAS FILTRABLES PARA COMBOBOX ===
    private FilteredList<Materia> materiasFiltradas;
    private FilteredList<Maestro> maestrosFiltradas;

    // === SERVICIOS Y ESTADO ===
    private final GrupoService grupoService = new GrupoService();
    private final MateriaService materiaService = new MateriaService();
    private final MaestroService maestroService = new MaestroService();
    private final CargaDatosService cargaDatosService = new CargaDatosService();
    
    private final ObservableList<Grupo> listaGrupos = FXCollections.observableArrayList();
    private FilteredList<Grupo> gruposFiltrados;
    private Grupo grupoEnEdicion = null;
    private Runnable accionPendiente;
    private final int FILAS_POR_PAGINA = 15;

    @FXML
    public void initialize() {
        tablaGrupos.setFixedCellSize(48);
        configurarColumnas();
        cargarCatalogos();
        configurarBusquedaCombos();
        cargarDatos();
    }

    private void configurarColumnas() {
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materiaNombre"));
        colMaestro.setCellValueFactory(new PropertyValueFactory<>("maestroNombre"));
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colEstado.setCellFactory(param -> new TableCell<>() {
            private final Label lblBadge = new Label();
            {
                lblBadge.setStyle("-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
            }
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) {
                    setGraphic(null);
                } else {
                    lblBadge.setText(activo ? "ACTIVO" : "INACTIVO");
                    // Usamos colores suaves para los badges
                    lblBadge.setStyle(lblBadge.getStyle() + (activo 
                        ? "-fx-background-color: #d4edda; -fx-text-fill: #155724;" 
                        : "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;"));
                    setGraphic(lblBadge);
                }
            }
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstado = new Button();
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox panel = new HBox(8, btnEditar, btnEstado, btnEliminar);

            {
                // Usamos las clases de AtlantaFX para que se vean modernos pero con texto
                btnEditar.getStyleClass().addAll("flat", "accent");
                btnEstado.getStyleClass().addAll("flat");
                btnEliminar.getStyleClass().addAll("flat", "danger");
                
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
                    Grupo g = getTableView().getItems().get(getIndex());
                    
                    // Retornamos al texto explícito
                    btnEstado.setText(g.isActivo() ? "Desactivar" : "Activar");
                    
                    // Mantenemos el cambio de color dinámico
                    btnEstado.getStyleClass().removeAll("success", "warning");
                    btnEstado.getStyleClass().add(g.isActivo() ? "warning" : "success");
                    
                    setGraphic(panel);
                }
            }
        });

        tablaGrupos.setRowFactory(tv -> {
            TableRow<Grupo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (! row.isEmpty()) ) {
                    abrirEdicion(row.getItem());
                }
            });
            return row ;
        });
    }

    // ==========================================
    // CARGA Y FILTRADO DE DATOS
    // ==========================================

    private void cargarCatalogos() {
        try {
            List<Materia> todasMaterias = materiaService.listarTodas();
            materiasFiltradas = new FilteredList<>(FXCollections.observableArrayList(todasMaterias), p -> true);
            cbMateria.setItems(materiasFiltradas);

            List<Maestro> todosMaestros = maestroService.listarTodos();
            maestrosFiltradas = new FilteredList<>(FXCollections.observableArrayList(todosMaestros), p -> true);
            cbMaestro.setItems(maestrosFiltradas);
        } catch (Exception e) {
            mostrarNotificacion("Error al cargar catálogos.", true);
        }
    }

    private void configurarBusquedaCombos() {
        configurarFiltroPersonalizado(cbMateria, materiasFiltradas);
        configurarFiltroPersonalizado(cbMaestro, maestrosFiltradas);
    }

    private <T> void configurarFiltroPersonalizado(ComboBox<T> combo, FilteredList<T> listaFiltrada) {
        combo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                if (newVal == null || (combo.getSelectionModel().getSelectedItem() != null 
                    && combo.getSelectionModel().getSelectedItem().toString().equals(newVal))) {
                    return;
                }
                listaFiltrada.setPredicate(item -> {
                    if (newVal.isEmpty()) return true;
                    return item.toString().toLowerCase().contains(newVal.toLowerCase());
                });
                if (!listaFiltrada.isEmpty()) combo.show();
                else combo.hide();
            });
        });
    }

    private void cargarDatos() {
        try {
            List<Grupo> grupos = grupoService.listarTodos();
            listaGrupos.setAll(grupos);
            gruposFiltrados = new FilteredList<>(listaGrupos, p -> true);
            handleBusqueda();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML
    private void handleBusqueda() {
        String filtro = campoBusqueda.getText().toLowerCase().trim();
        gruposFiltrados.setPredicate(g -> {
            if (filtro.isEmpty()) return true;
            return g.getClave().toLowerCase().contains(filtro) || 
                   g.getMateriaNombre().toLowerCase().contains(filtro) ||
                   g.getMaestroNombre().toLowerCase().contains(filtro);
        });
        configurarPaginacion();
    }

    private void configurarPaginacion() {
        int total = gruposFiltrados.size();
        int paginas = (int) Math.ceil((double) total / FILAS_POR_PAGINA);
        paginacionGrupos.setPageCount(paginas > 0 ? paginas : 1);
        paginacionGrupos.setPageFactory(idx -> {
            int desde = idx * FILAS_POR_PAGINA;
            int hasta = Math.min(desde + FILAS_POR_PAGINA, total);
            tablaGrupos.setItems(FXCollections.observableArrayList(gruposFiltrados.subList(desde, hasta)));
            return new Region();
        });
    }

    // ==========================================
    // OPERACIONES CRUD Y ARCHIVOS
    // ==========================================

    @FXML
    private void handleImportarCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo CSV de Grupos");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        
        File file = fileChooser.showOpenDialog(tablaGrupos.getScene().getWindow());
        if (file != null) {
            try (InputStream is = new FileInputStream(file)) {
                List<String> errores = cargaDatosService.importarGruposCsv(is);
                
                if (errores.isEmpty()) {
                    mostrarNotificacion("Archivo CSV importado exitosamente.", false);
                } else {
                    mostrarNotificacion("Importación completada con " + errores.size() + " errores. Revisa la consola.", true);
                    // Opcional: Imprimir errores en consola para revisión técnica
                    errores.forEach(System.err::println);
                }
                cargarDatos(); // Refrescamos la tabla
            } catch (Exception e) {
                mostrarNotificacion("Error al procesar el archivo: " + e.getMessage(), true);
            }
        }
    }

    @FXML
    private void handleNuevo() {
        grupoEnEdicion = null;
        limpiarFormulario();
        labelTituloFormulario.setText("Nuevo Grupo Académico");
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    private void abrirEdicion(Grupo g) {
        grupoEnEdicion = g;
        campoClave.setText(g.getClave());
        campoSemestre.setText(g.getSemestre());
        
        cbMateria.getItems().stream().filter(m -> m.getId() == g.getMateriaId()).findFirst().ifPresent(cbMateria::setValue);
        cbMaestro.getItems().stream().filter(m -> m.getId() == g.getMaestroId()).findFirst().ifPresent(cbMaestro::setValue);
        
        labelTituloFormulario.setText("Editar Grupo");
        panelFormulario.setVisible(true);
        panelFormulario.setManaged(true);
    }

    @FXML
    private void handleGuardar() {
        try {
            Materia mat = cbMateria.getValue();
            Maestro mae = cbMaestro.getValue();
            if (mat == null || mae == null) throw new IllegalArgumentException("Seleccione materia y docente.");

            Grupo g = (grupoEnEdicion != null) ? grupoEnEdicion : new Grupo();
            g.setMateriaId(mat.getId());
            g.setMaestroId(mae.getId());
            g.setClave(campoClave.getText().trim());
            g.setSemestre(campoSemestre.getText().trim());
            g.setActivo(true);

            // Ahora enviamos el parámetro esEdicion
            grupoService.guardar(g, grupoEnEdicion != null);
            
            mostrarNotificacion("Grupo guardado con éxito.", false);
            handleCancelar();
            cargarDatos();
        } catch (Exception e) {
            mostrarNotificacion(e.getMessage(), true);
        }
    }

    @FXML private void handleCancelar() { panelFormulario.setVisible(false); panelFormulario.setManaged(false); }
    private void limpiarFormulario() { campoClave.clear(); campoSemestre.clear(); cbMateria.setValue(null); cbMaestro.setValue(null); }

    // ==========================================
    // NOTIFICACIONES Y CONFIRMACIONES
    // ==========================================

    private void confirmarCambioEstado(Grupo g) {
        boolean nuevo = !g.isActivo();
        mostrarConfirmacion("Confirmar Estado", "¿Deseas " + (nuevo ? "activar" : "desactivar") + " el grupo " + g.getClave() + "?", nuevo ? "success" : "warning", () -> {
            try {
                // Cables conectados al servicio
                grupoService.cambiarEstado(g.getId(), nuevo);
                cargarDatos();
                mostrarNotificacion("Estado actualizado exitosamente.", false);
            } catch (Exception e) { mostrarNotificacion(e.getMessage(), true); }
        });
    }

    private void confirmarEliminacion(Grupo g) {
        mostrarConfirmacion("Eliminar Grupo", "¿Deseas eliminar permanentemente el grupo " + g.getClave() + "?", "danger", () -> {
            try {
                // Cables conectados al servicio
                grupoService.eliminar(g.getId());
                cargarDatos();
                mostrarNotificacion("Grupo eliminado exitosamente.", false);
            } catch (Exception e) { mostrarNotificacion(e.getMessage(), true); }
        });
    }

    private void mostrarNotificacion(String msj, boolean error) {
        mensajeGeneral.setText(msj);
        mensajeGeneral.setVisible(true); mensajeGeneral.setManaged(true);
        mensajeGeneral.setStyle("-fx-background-color: " + (error ? "#f8d7da" : "#d4edda") + "; -fx-text-fill: " + (error ? "#721c24" : "#155724") + "; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(2)); fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> { mensajeGeneral.setVisible(false); mensajeGeneral.setManaged(false); });
        fade.play();
    }

    private void mostrarConfirmacion(String tit, String msj, String clase, Runnable acc) {
        lblTituloConfirmacion.setText(tit); lblMensajeConfirmacion.setText(msj);
        btnConfirmarAccion.setText("Confirmar"); btnConfirmarAccion.getStyleClass().setAll("button", clase);
        this.accionPendiente = acc; panelConfirmacion.setVisible(true); panelConfirmacion.setManaged(true);
    }

    @FXML private void handleCancelarConfirmacion() { panelConfirmacion.setVisible(false); panelConfirmacion.setManaged(false); }
    @FXML private void handleEjecutarConfirmacion() { if (accionPendiente != null) accionPendiente.run(); handleCancelarConfirmacion(); }
}