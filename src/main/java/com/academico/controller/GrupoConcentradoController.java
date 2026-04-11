package com.academico.controller;

import com.academico.model.*;
import com.academico.service.CalificacionService;
import com.academico.service.ReporteService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.UnidadService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class GrupoConcentradoController {

    // === SERVICIOS (Arquitectura Limpia) ===
    private final ReporteService reporteService = new ReporteService(); // Orquestador principal
    private final InscripcionService inscripcionService = new InscripcionService(); // Para Overrides
    private final GrupoService grupoService = new GrupoService(); // Para Cierre de Curso
    
    // Servicios auxiliares solo para la construcción visual de la UI
    private final UnidadService unidadService = new UnidadService(); 
    private final CalificacionService calificacionService = new CalificacionService();

    // === ELEMENTOS UI ===
    @FXML private Label lblEstadoCurso;
    @FXML private Button btnCerrarCurso;
    @FXML private TextField campoBusqueda;
    @FXML private TableView<CalificacionFinal> tablaConcentrado; // Usamos el DTO directamente
    
    @FXML private VBox panelOverride;
    @FXML private Label lblNombreOverride;
    @FXML private TextField campoCalificacionManual;
    @FXML private TextField campoMotivoOverride;
    @FXML private Label lblMensajeOverride;

    // === ESTADO ===
    private Grupo grupoActual;
    private CalificacionFinal alumnoSeleccionado;
    private boolean cursoCerrado = false; 
    
    private final ObservableList<CalificacionFinal> listaDatos = FXCollections.observableArrayList();
    private FilteredList<CalificacionFinal> datosFiltrados;
    private List<Unidad> unidadesGrupo;

    @FXML
    public void initialize() {
        if (DashboardMaestroController.instancia != null) {
            grupoActual = DashboardMaestroController.instancia.getGrupoSeleccionado();
            this.cursoCerrado = grupoActual.isCerrado(); // Estado real desde la BD
        }
        actualizarUICursoCerrado();
        cargarDatos();
    }

    private void actualizarUICursoCerrado() {
        if (cursoCerrado) {
            lblEstadoCurso.setText("CERRADO");
            lblEstadoCurso.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #cf222e; -fx-background-color: #ffebe9; -fx-padding: 5 15; -fx-background-radius: 15;");
            btnCerrarCurso.setDisable(true);
            btnCerrarCurso.setText("Acta Firmada");
        }
    }

    // ==========================================
    // CARGA DE DATOS (Delegada al ReporteService)
    // ==========================================
    private void cargarDatos() {
        try {
            listaDatos.clear();
            // Necesitamos las unidades solo para saber cuántas columnas dibujar
            unidadesGrupo = unidadService.listarPorMateria(grupoActual.getMateriaId());
            
            // El ReporteService hace todo el trabajo pesado de BD y cálculos
            List<CalificacionFinal> reporte = reporteService.generarReporteFinalGrupo(grupoActual.getId());
            listaDatos.addAll(reporte);

            datosFiltrados = new FilteredList<>(listaDatos, p -> true);
            construirColumnas();
            tablaConcentrado.setItems(datosFiltrados);

        } catch (Exception e) {
            System.err.println("Error al cargar concentrado: " + e.getMessage());
        }
    }

    // ==========================================
    // COLUMNAS DINÁMICAS (Consumiendo DTO)
    // ==========================================
    private void construirColumnas() {
        tablaConcentrado.getColumns().clear();
        tablaConcentrado.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // 1. Matrícula
        TableColumn<CalificacionFinal, String> colMatricula = new TableColumn<>();
        Label lblMat = new Label("Matrícula"); lblMat.setMaxWidth(Double.MAX_VALUE); lblMat.setAlignment(Pos.CENTER);
        colMatricula.setGraphic(lblMat);
        colMatricula.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlumnoMatricula()));
        colMatricula.setPrefWidth(100); colMatricula.setResizable(false); colMatricula.setReorderable(false);
        colMatricula.setStyle("-fx-alignment: CENTER;");

        // 2. Nombre
        TableColumn<CalificacionFinal, String> colNombre = new TableColumn<>("Nombre del Alumno");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAlumnoNombre()));
        colNombre.setResizable(false); colNombre.setReorderable(false);

        tablaConcentrado.getColumns().add(colMatricula);
        tablaConcentrado.getColumns().add(colNombre);

        double anchoDinamico = 0;

        // 3. Unidades Dinámicas
        for (Unidad unidad : unidadesGrupo) {
            TableColumn<CalificacionFinal, String> colU = new TableColumn<>();
            Label lblU = new Label("U" + unidad.getNumero()); lblU.setMaxWidth(Double.MAX_VALUE); lblU.setAlignment(Pos.CENTER);
            colU.setGraphic(lblU);
            
            colU.setCellValueFactory(d -> {
                Optional<ResultadoUnidad> ru = d.getValue().getUnidades().stream()
                        .filter(u -> u.getUnidadId() == unidad.getId()).findFirst();
                return new SimpleStringProperty(ru.map(r -> r.getResultadoFinal() != null ? r.getResultadoFinal().toString() : "-").orElse("-"));
            });
            colU.setPrefWidth(60); colU.setResizable(false); colU.setReorderable(false);
            colU.setStyle("-fx-alignment: CENTER;");
            tablaConcentrado.getColumns().add(colU);
            anchoDinamico += 60;
        }

        // 4. Promedio Base
        TableColumn<CalificacionFinal, String> colProm = new TableColumn<>();
        Label lblProm = new Label("Promedio\nBase"); lblProm.setMaxWidth(Double.MAX_VALUE); lblProm.setAlignment(Pos.CENTER); lblProm.setTextAlignment(TextAlignment.CENTER);
        colProm.setGraphic(lblProm);
        colProm.setCellValueFactory(d -> {
            BigDecimal val = d.getValue().getCalificacionCalculada();
            return new SimpleStringProperty(val != null ? val.toString() : "-");
        });
        colProm.setPrefWidth(100); colProm.setResizable(false); colProm.setReorderable(false);
        colProm.setStyle("-fx-alignment: CENTER;");

        // 5. Calificación Definitiva
        TableColumn<CalificacionFinal, String> colDef = new TableColumn<>();
        Label lblDef = new Label("Calif.\nFinal"); lblDef.setMaxWidth(Double.MAX_VALUE); lblDef.setAlignment(Pos.CENTER); lblDef.setTextAlignment(TextAlignment.CENTER);
        colDef.setGraphic(lblDef);
        colDef.setCellValueFactory(d -> {
            BigDecimal val = d.getValue().getCalificacionFinal();
            String valor = val != null ? val.toString() : "-";
            if (d.getValue().isEsOverride()) valor += " (M)"; 
            return new SimpleStringProperty(valor);
        });
        colDef.setPrefWidth(100); colDef.setResizable(false); colDef.setReorderable(false);
        colDef.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #f6f8fa;");

        // 6. Estado (Dinámico con ConfiguracionService a través de CalificacionService)
        TableColumn<CalificacionFinal, String> colEstado = new TableColumn<>();
        Label lblEstado = new Label("Estado"); lblEstado.setMaxWidth(Double.MAX_VALUE); lblEstado.setAlignment(Pos.CENTER);
        colEstado.setGraphic(lblEstado);
        colEstado.setCellValueFactory(d -> {
            try {
                return new SimpleStringProperty(calificacionService.determinarEstado(d.getValue().getCalificacionFinal()));
            } catch (Exception e) { return new SimpleStringProperty("ERROR"); }
        });

        colEstado.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    badge.setText(item);
                    if (item.equals("APROBADO")) {
                        badge.setStyle("-fx-background-color: #dafbe1; -fx-text-fill: #1a7f37; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15;");
                    } else if (item.equals("REPROBADO")) {
                        badge.setStyle("-fx-background-color: #ffebe9; -fx-text-fill: #cf222e; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15;");
                    } else {
                        badge.setStyle("-fx-background-color: #f6f8fa; -fx-text-fill: #57606a; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 15;");
                    }
                    setGraphic(badge); setStyle("-fx-alignment: CENTER;");
                }
            }
        });
        colEstado.setPrefWidth(115); colEstado.setResizable(false); colEstado.setReorderable(false);

        // 7. Acciones
        TableColumn<CalificacionFinal, CalificacionFinal> colAcciones = new TableColumn<>();
        Label lblAcc = new Label("Auditoría"); lblAcc.setMaxWidth(Double.MAX_VALUE); lblAcc.setAlignment(Pos.CENTER);
        colAcciones.setGraphic(lblAcc);
        colAcciones.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        colAcciones.setCellFactory(p -> new TableCell<>() {
            private final Button btnDesglose = new Button("Detalles");
            private final Button btnOverride = new Button("Override");
            private final HBox panel = new HBox(5, btnDesglose, btnOverride);
            {
                panel.setStyle("-fx-alignment: CENTER;");
                btnDesglose.getStyleClass().add("flat");
                btnDesglose.setOnAction(e -> mostrarDesglose(getItem()));
                btnOverride.getStyleClass().addAll("flat", "danger");
                btnOverride.setOnAction(e -> prepararOverride(getItem()));
            }
            @Override
            protected void updateItem(CalificacionFinal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    btnOverride.setDisable(cursoCerrado);
                    setGraphic(panel);
                }
            }
        });
        colAcciones.setPrefWidth(200); colAcciones.setResizable(false); colAcciones.setReorderable(false);

        tablaConcentrado.getColumns().add(colProm);
        tablaConcentrado.getColumns().add(colDef);
        tablaConcentrado.getColumns().add(colEstado);
        tablaConcentrado.getColumns().add(colAcciones);

        double anchoFijo = 100 + anchoDinamico + 100 + 100 + 115 + 200 + 3;
        colNombre.prefWidthProperty().bind(tablaConcentrado.widthProperty().subtract(anchoFijo));
    }

    // ==========================================
    // LÓGICA DE OVERRIDE Y CIERRE (PERSISTENCIA REAL)
    // ==========================================
    @FXML private void handleBusqueda() {
        String texto = campoBusqueda.getText().toLowerCase().trim();
        datosFiltrados.setPredicate(f -> texto.isEmpty() || 
            f.getAlumnoNombre().toLowerCase().contains(texto) ||
            f.getAlumnoMatricula().toLowerCase().contains(texto));
    }

    @FXML private void handleCerrarCurso() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cierre Definitivo de Curso");
        alert.setHeaderText("Estás a punto de cerrar el acta de la materia.");
        alert.setContentText("Una vez cerrado, el grupo se archivará y no podrás modificar calificaciones ni realizar overrides. ¿Deseas continuar?");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                // Llama al servicio real que actualiza 'estado_evaluacion' y 'activo'
                grupoService.cerrarCursoDefinitivamente(grupoActual.getId());
                
                cursoCerrado = true;
                grupoActual.setEstadoEvaluacion("CERRADO");
                grupoActual.setActivo(false); // Reflejar en memoria
                
                actualizarUICursoCerrado();
                cargarDatos(); 
            } catch (Exception e) {
                Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage());
                error.showAndWait();
            }
        }
    }

    private void prepararOverride(CalificacionFinal fila) {
        if (cursoCerrado) return;
        alumnoSeleccionado = fila;
        lblNombreOverride.setText(fila.getAlumnoMatricula() + " - " + fila.getAlumnoNombre());
        campoCalificacionManual.clear();
        campoMotivoOverride.clear();
        lblMensajeOverride.setVisible(false);
        panelOverride.setVisible(true); panelOverride.setManaged(true);
    }

    @FXML private void handleCancelarOverride() {
        panelOverride.setVisible(false); panelOverride.setManaged(false);
        alumnoSeleccionado = null;
    }

    @FXML private void handleGuardarOverride() {
        String valStr = campoCalificacionManual.getText().trim();
        String motivo = campoMotivoOverride.getText().trim();

        if (valStr.isEmpty() || motivo.isEmpty() || motivo.length() < 10) {
            mostrarMensaje("Ingresa un valor numérico y una justificación detallada (min. 10 caracteres).", true);
            return;
        }

        try {
            BigDecimal valor = new BigDecimal(valStr);
            
            // Llamada al servicio real en lugar de guardar en Map
            inscripcionService.aplicarOverrideMateria(
                alumnoSeleccionado.getInscripcionId(), 
                valor, 
                motivo
            );

            mostrarMensaje("Calificación manual aplicada correctamente.", false);
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
            pause.setOnFinished(e -> { handleCancelarOverride(); cargarDatos(); });
            pause.play();

        } catch (NumberFormatException e) {
            mostrarMensaje("Formato numérico inválido.", true);
        } catch (Exception e) {
            mostrarMensaje(e.getMessage(), true);
        }
    }

    private void mostrarMensaje(String msj, boolean error) {
        lblMensajeOverride.setText(msj);
        lblMensajeOverride.setVisible(true); lblMensajeOverride.setManaged(true);
        lblMensajeOverride.setStyle(error ? "-fx-text-fill: #cf222e;" : "-fx-text-fill: #155724;");
    }

    private void mostrarDesglose(CalificacionFinal cf) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Auditoría de Calificación");
        info.setHeaderText("Desglose para: " + cf.getAlumnoNombre());
        
        StringBuilder sb = new StringBuilder();
        sb.append("Promedio Calculado (Unidades): ").append(cf.getCalificacionCalculada() != null ? cf.getCalificacionCalculada() : "N/A").append("\n");
        sb.append("Bonus aplicado a la Materia: +").append(cf.getBonusMateria()).append("\n");
        sb.append("Calificación con Bonus: ").append(cf.getCalificacionConBonus() != null ? cf.getCalificacionConBonus() : "N/A").append("\n\n");
        
        if (cf.isEsOverride()) {
            sb.append("ESTADO DE EXCEPCIÓN (OVERRIDE) ACTIVO\n");
            sb.append("Calificación Asignada: ").append(cf.getCalificacionFinal()).append("\n");
            sb.append("Justificación: ").append(cf.getOverrideJustificacion()).append("\n");
        } else {
            sb.append("Calificación Final en Acta: ").append(cf.getCalificacionFinal() != null ? cf.getCalificacionFinal() : "N/A").append("\n");
        }
        
        info.setContentText(sb.toString());
        info.showAndWait();
    }
}