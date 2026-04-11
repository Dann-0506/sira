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
import javafx.geometry.Pos;
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
    @FXML private TableColumn<Grupo, String> colClave, colMateria, colMaestro, colSemestre, colEstadoEvaluacion;
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
        tablaGrupos.getColumns().clear();
        tablaGrupos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 1. CLAVE (Centrado - Limpiamos Texto)
        colClave.setText(""); // Borra el texto del FXML para evitar duplicados
        Label lblClave = new Label("Clave");
        lblClave.setMaxWidth(Double.MAX_VALUE); lblClave.setAlignment(Pos.CENTER);
        colClave.setGraphic(lblClave);
        colClave.setCellValueFactory(new PropertyValueFactory<>("clave"));
        colClave.setPrefWidth(110); colClave.setResizable(false); colClave.setReorderable(false);
        colClave.setStyle("-fx-alignment: CENTER;");

        // 2. MATERIA (A la izquierda - Solo Texto)
        colMateria.setGraphic(null); 
        colMateria.setText("Materia");
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materiaNombre"));
        colMateria.setResizable(false); colMateria.setReorderable(false);

        // 3. MAESTRO (A la izquierda - Solo Texto)
        colMaestro.setGraphic(null);
        colMaestro.setText("Maestro");
        colMaestro.setCellValueFactory(new PropertyValueFactory<>("maestroNombre"));
        colMaestro.setPrefWidth(220); colMaestro.setResizable(false); colMaestro.setReorderable(false);

        // 4. SEMESTRE (Centrado - Limpiamos Texto)
        colSemestre.setText(""); 
        Label lblSem = new Label("Semestre");
        lblSem.setMaxWidth(Double.MAX_VALUE); lblSem.setAlignment(Pos.CENTER);
        colSemestre.setGraphic(lblSem);
        colSemestre.setCellValueFactory(new PropertyValueFactory<>("semestre"));
        colSemestre.setPrefWidth(120); colSemestre.setResizable(false); colSemestre.setReorderable(false);
        colSemestre.setStyle("-fx-alignment: CENTER;");

        // 5. EVALUACIÓN (Centrado - Limpiamos Texto)
        colEstadoEvaluacion.setText("");
        Label lblEval = new Label("Evaluación");
        lblEval.setMaxWidth(Double.MAX_VALUE); lblEval.setAlignment(Pos.CENTER);
        colEstadoEvaluacion.setGraphic(lblEval);
        colEstadoEvaluacion.setCellValueFactory(new PropertyValueFactory<>("estadoEvaluacion"));
        colEstadoEvaluacion.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    badge.setText(item);
                    String estilo = "CERRADO".equals(item) 
                        ? "-fx-background-color: #ffebe9; -fx-text-fill: #cf222e;" 
                        : "-fx-background-color: #ddf4ff; -fx-text-fill: #0969da;";
                    badge.setStyle(estilo + "-fx-font-weight: bold; -fx-padding: 3 12; -fx-background-radius: 12; -fx-font-size: 11px;");
                    setGraphic(badge); setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        colEstadoEvaluacion.setPrefWidth(130); colEstadoEvaluacion.setResizable(false); colEstadoEvaluacion.setReorderable(false);

        // 6. ESTADO (Centrado - Limpiamos Texto)
        colEstado.setText("");
        Label lblEstadoBadge = new Label("Estado");
        lblEstadoBadge.setMaxWidth(Double.MAX_VALUE); lblEstadoBadge.setAlignment(Pos.CENTER);
        colEstado.setGraphic(lblEstadoBadge);
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colEstado.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(Boolean activo, boolean empty) {
                super.updateItem(activo, empty);
                if (empty || activo == null) setGraphic(null);
                else {
                    badge.setText(activo ? "ACTIVO" : "INACTIVO");
                    String estilo = activo 
                        ? "-fx-background-color: #d4edda; -fx-text-fill: #155724;" 
                        : "-fx-background-color: #e2e3e5; -fx-text-fill: #383d41;";
                    badge.setStyle(estilo + "-fx-font-weight: bold; -fx-padding: 3 12; -fx-background-radius: 12; -fx-font-size: 11px;");
                    setGraphic(badge); setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        colEstado.setPrefWidth(120); colEstado.setResizable(false); colEstado.setReorderable(false);

        // 7. ACCIONES (Centrado - Limpiamos Texto)
        colAcciones.setText("");
        Label lblAcc = new Label("Acciones");
        lblAcc.setMaxWidth(Double.MAX_VALUE); lblAcc.setAlignment(Pos.CENTER);
        colAcciones.setGraphic(lblAcc);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEstadoAccion = new Button();
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnReabrir = new Button("Reabrir Acta");
            private final HBox panel = new HBox(8, btnEditar, btnEstadoAccion, btnReabrir, btnEliminar);
            {
                panel.setStyle("-fx-alignment: center;");
                btnEditar.getStyleClass().addAll("flat", "accent");
                btnEstadoAccion.getStyleClass().addAll("flat");
                btnReabrir.getStyleClass().addAll("flat", "warning");
                btnEliminar.getStyleClass().addAll("flat", "danger");

                btnReabrir.setOnAction(e -> { Grupo g = getTableRow().getItem(); if (g != null) confirmarReapertura(g); });
                btnEditar.setOnAction(e -> { Grupo g = getTableRow().getItem(); if (g != null) abrirEdicion(g); });
                btnEstadoAccion.setOnAction(e -> { Grupo g = getTableRow().getItem(); if (g != null) confirmarCambioEstado(g); });
                btnEliminar.setOnAction(e -> { Grupo g = getTableRow().getItem(); if (g != null) confirmarEliminacion(g); });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setGraphic(null);
                else {
                    Grupo g = getTableRow().getItem();
                    btnEstadoAccion.setText(g.isActivo() ? "Desactivar" : "Activar");
                    btnEstadoAccion.getStyleClass().removeAll("success", "warning");
                    btnEstadoAccion.getStyleClass().add(g.isActivo() ? "warning" : "success");
                    btnReabrir.setVisible(g.isCerrado());
                    btnReabrir.setManaged(g.isCerrado());
                    setGraphic(panel);
                }
            }
        });
        colAcciones.setPrefWidth(400); colAcciones.setResizable(false); colAcciones.setReorderable(false);

        // Re-añadir columnas
        tablaGrupos.getColumns().add(colClave);
        tablaGrupos.getColumns().add(colMateria);
        tablaGrupos.getColumns().add(colMaestro);
        tablaGrupos.getColumns().add(colSemestre);
        tablaGrupos.getColumns().add(colEstadoEvaluacion);
        tablaGrupos.getColumns().add(colEstado);
        tablaGrupos.getColumns().add(colAcciones);

        // Cálculo de ancho
        double anchoFijo = 110 + 220 + 120 + 130 + 120 + 400 + 10; 
        colMateria.prefWidthProperty().bind(tablaGrupos.widthProperty().subtract(anchoFijo));
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
        // 1. EL SALVAVIDAS: Le enseñamos a JavaFX a convertir el Texto (String) de vuelta al Objeto (T)
        combo.setConverter(new javafx.util.StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? "" : object.toString();
            }

            @Override
            public T fromString(String string) {
                // Busca en la lista el objeto que coincida exactamente con el texto escrito
                return combo.getItems().stream()
                        .filter(item -> item.toString().equals(string))
                        .findFirst().orElse(null);
            }
        });

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

                // AJUSTE: Solo desplegar si el usuario tiene el foco en el componente
                if (!listaFiltrada.isEmpty() && combo.getEditor().isFocused()) {
                    combo.show();
                } else {
                    combo.hide();
                }
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
            tablaGrupos.refresh();
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
                    mostrarNotificacion("Importación completada con " + errores.size() + " errores.", true);
                    mostrarDetallesErrores(errores, tablaGrupos.getScene().getWindow());
                }
                cargarDatos();
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
        limpiarFormulario();
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

    @FXML private void handleCancelar() { 
        panelFormulario.setVisible(false); 
        panelFormulario.setManaged(false); 
        limpiarFormulario(); // Llama a la limpieza reforzada
    }
    
    private void limpiarFormulario() { 
        // 1. Forzamos el cierre visual
        cbMateria.hide();
        cbMaestro.hide();

        // 2. Limpiamos campos de texto
        campoClave.clear(); 
        campoSemestre.clear(); 
        
        // 3. Limpiamos selección y editores para que el listener no se dispare
        cbMateria.setValue(null); 
        cbMaestro.setValue(null); 
        cbMateria.getEditor().clear();
        cbMaestro.getEditor().clear();
    }

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

    private void confirmarReapertura(Grupo g) {
        mostrarConfirmacion(
            "Reapertura de Curso", 
            "¿Estás seguro de reabrir el acta del grupo " + g.getClave() + "?\n" +
            "Esto permitirá que el docente vuelva a modificar calificaciones.", 
            "warning", 
            () -> {
                try {
                    grupoService.reabrirCurso(g.getId());
                    cargarDatos(); // Refrescar tabla
                    mostrarNotificacion("El curso ha sido reabierto exitosamente.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
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