package com.academico.controller;

import com.academico.model.*;
import com.academico.service.CalificacionService;
import com.academico.service.individuals.*;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrupoCalificacionesController {

    // === SERVICIOS ===
    private final UnidadService unidadService = new UnidadService();
    private final ActividadGrupoService actividadService = new ActividadGrupoService();
    private final AlumnoService alumnoService = new AlumnoService();
    private final InscripcionService inscripcionService = new InscripcionService();
    private final ResultadoService resultadoService = new ResultadoService();
    private final CalificacionService calificacionService = new CalificacionService();

    // === ELEMENTOS UI ===
    @FXML private ComboBox<Unidad> comboUnidades;
    @FXML private Button btnGuardar;
    @FXML private HBox panelAdvertencia;
    @FXML private Label lblAdvertencia;
    @FXML private TableView<FilaCalificacion> tablaCalificaciones;

    // === ESTADO ===
    private Grupo grupoActual;
    private List<ActividadGrupo> actividadesUnidad;
    private ObservableList<FilaCalificacion> datosTabla = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (DashboardMaestroController.instancia != null) {
            grupoActual = DashboardMaestroController.instancia.getGrupoSeleccionado();
        }

        cargarUnidades();

        comboUnidades.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                prepararTablaParaUnidad(newVal);
            }
        });

        tablaCalificaciones.getSelectionModel().setCellSelectionEnabled(true);
    }

    private void cargarUnidades() {
        if (grupoActual == null) return;
        try {
            List<Unidad> unidades = unidadService.listarPorMateria(grupoActual.getMateriaId());
            comboUnidades.setItems(FXCollections.observableArrayList(unidades));
        } catch (Exception e) {
            mostrarAdvertencia("Error al cargar unidades: " + e.getMessage(), true);
        }
    }

    private void prepararTablaParaUnidad(Unidad unidad) {
        tablaCalificaciones.getColumns().clear();
        datosTabla.clear();
        
        try {
            actividadesUnidad = actividadService.buscarPorGrupoYUnidad(grupoActual.getId(), unidad.getId());
            
            if (!calificacionService.ponderacionesValidas(actividadesUnidad)) {
                mostrarAdvertencia("🔒 Rúbrica incompleta. La suma de las actividades no es 100%. Ve a la pestaña 'Rúbrica' para configurarlo.", true);
                tablaCalificaciones.setDisable(true);
                btnGuardar.setDisable(true);
                return;
            }

            panelAdvertencia.setVisible(false);
            panelAdvertencia.setManaged(false);
            tablaCalificaciones.setDisable(false);
            btnGuardar.setDisable(false);

            construirColumnas();

            cargarDatosAlumnos(unidad.getId());

        } catch (Exception e) {
            mostrarAdvertencia("Error al preparar la tabla: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // CONSTRUCCIÓN DINÁMICA DE LA TABLA
    // ==========================================

    private void construirColumnas() {
        tablaCalificaciones.getColumns().clear();
        tablaCalificaciones.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // -- COLUMNA: MATRÍCULA --
        TableColumn<FilaCalificacion, String> colMatricula = new TableColumn<>();
        Label lblMatricula = new Label("Matrícula");
        lblMatricula.setMaxWidth(Double.MAX_VALUE);
        lblMatricula.setAlignment(Pos.CENTER);
        colMatricula.setGraphic(lblMatricula);
        
        colMatricula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlumno().getMatricula()));
        colMatricula.setPrefWidth(120); colMatricula.setMinWidth(120); colMatricula.setMaxWidth(120);
        colMatricula.setResizable(false); 
        colMatricula.setReorderable(false);

        // -- COLUMNA: ALUMNO --
        TableColumn<FilaCalificacion, String> colAlumno = new TableColumn<>("Alumno");
        colAlumno.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlumno().getNombre()));
        colAlumno.setResizable(false);
        colAlumno.setReorderable(false);

        tablaCalificaciones.getColumns().add(colMatricula);
        tablaCalificaciones.getColumns().add(colAlumno);

        double anchoDinamico = 0;

        // -- COLUMNAS: ACTIVIDADES --
        for (ActividadGrupo actividad : actividadesUnidad) {
            TableColumn<FilaCalificacion, String> colActividad = new TableColumn<>();

            Label lblActividad = new Label(actividad.getNombre() + "\n(" + actividad.getPonderacion() + "%)");
            lblActividad.setMaxWidth(Double.MAX_VALUE);
            lblActividad.setAlignment(Pos.CENTER);
            lblActividad.setTextAlignment(TextAlignment.CENTER);
            colActividad.setGraphic(lblActividad);

            colActividad.setCellValueFactory(data -> data.getValue().getCalificacionProperty(actividad.getId()));

            colActividad.setCellFactory(param -> new TableCell<>() {
                private final TextField textField = new TextField();

                {
                    textField.setStyle("-fx-background-color: transparent; -fx-alignment: center;");

                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            commitEdit(textField.getText());
                        }
                    });

                    textField.setOnKeyPressed(event -> {
                        switch (event.getCode()) {
                            case ENTER:
                                commitEdit(textField.getText());
                                tablaCalificaciones.getSelectionModel().selectBelowCell();
                                break;
                            case UP:
                                tablaCalificaciones.getSelectionModel().selectAboveCell();
                                event.consume();
                                break;
                            case DOWN:
                                tablaCalificaciones.getSelectionModel().selectBelowCell();
                                event.consume();
                                break;
                            default: break;
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        textField.setText(item);
                        setGraphic(textField);
                        
                        if (isFocused()) {
                            textField.requestFocus();
                        }
                    }
                }

                @Override
                public void commitEdit(String newValue) {
                    FilaCalificacion fila = getTableRow().getItem();
                    if (fila != null) {
                        String valorValidado = validarEntrada(newValue, getItem());
                        fila.setCalificacion(actividad.getId(), valorValidado);
                        recalcularResultadoBase(fila);
                        super.commitEdit(valorValidado);
                        textField.setText(valorValidado);
                    }
                }
            });

            colActividad.setPrefWidth(110); colActividad.setMinWidth(110); colActividad.setMaxWidth(110);
            colActividad.setResizable(false);
            colActividad.setReorderable(false);
            colActividad.setStyle("-fx-alignment: CENTER;");

            tablaCalificaciones.getColumns().add(colActividad);
            anchoDinamico += 110;
        }

        // -- COLUMNA: RESULTADO BASE --
        TableColumn<FilaCalificacion, String> colResultado = new TableColumn<>();
        Label lblResultado = new Label("Resultado Base");
        lblResultado.setMaxWidth(Double.MAX_VALUE);
        lblResultado.setAlignment(Pos.CENTER);
        colResultado.setGraphic(lblResultado);
        
        colResultado.setCellValueFactory(data -> data.getValue().resultadoBaseProperty());
        colResultado.setPrefWidth(130); colResultado.setMinWidth(130); colResultado.setMaxWidth(130);
        colResultado.setResizable(false); 
        colResultado.setReorderable(false);
        colResultado.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #f6f8fa;");

        tablaCalificaciones.getColumns().add(colResultado);

        double anchoRestanteFijo = 120 + 130 + anchoDinamico + 3;
        colAlumno.prefWidthProperty().bind(tablaCalificaciones.widthProperty().subtract(anchoRestanteFijo));
    }

    // ==========================================
    // CARGA Y CÁLCULO DE DATOS
    // ==========================================

    private void cargarDatosAlumnos(int unidadId) throws Exception {
        List<Alumno> alumnosInscritos = alumnoService.buscarPorGrupo(grupoActual.getId());
        List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoActual.getId());
        
        Map<Integer, Integer> mapaInscripciones = new HashMap<>();
        for (Inscripcion insc : inscripciones) {
            mapaInscripciones.put(insc.getAlumnoId(), insc.getId());
        }
        
        for (Alumno alumno : alumnosInscritos) {
            Integer inscripcionId = mapaInscripciones.get(alumno.getId());
            if (inscripcionId == null) continue;
            
            FilaCalificacion fila = new FilaCalificacion(alumno, inscripcionId);
            List<Resultado> resultadosPrevios = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidadId);
            
            for (Resultado r : resultadosPrevios) {
                fila.setCalificacion(r.getActividadGrupoId(), r.getCalificacion() != null ? r.getCalificacion().toString() : "");
            }
            
            // Reutilizamos nuestra función que ahora delega al servicio
            recalcularResultadoBase(fila);
            datosTabla.add(fila);
        }
        
        tablaCalificaciones.setItems(datosTabla);
    }

    private String validarEntrada(String nueva, String vieja) {
        try {
            if (nueva == null || nueva.trim().isEmpty()) return "";
            BigDecimal valor = new BigDecimal(nueva.trim());
            if (valor.compareTo(BigDecimal.ZERO) >= 0 && valor.compareTo(new BigDecimal("100")) <= 0) {
                return valor.setScale(2, RoundingMode.HALF_UP).toString();
            }
        } catch (NumberFormatException ignored) {}
        return vieja; // Si escribió letras o algo fuera de 0-100, regresa al valor anterior
    }

    private void recalcularResultadoBase(FilaCalificacion fila) {
        // 1. Preparamos los datos en el formato que el servicio espera
        List<Resultado> resultadosTemporales = new ArrayList<>();
        
        for (ActividadGrupo actividad : actividadesUnidad) {
            String calificacionStr = fila.getCalificacionValue(actividad.getId());
            if (!calificacionStr.isEmpty()) {
                Resultado r = new Resultado();
                r.setCalificacion(new BigDecimal(calificacionStr));
                r.setPonderacion(actividad.getPonderacion()); // Importante para que CalificacionService calcule la Aportación
                resultadosTemporales.add(r);
            }
        }

        // 2. Delegamos el cálculo al servicio
        BigDecimal totalUnidad = calificacionService.calcularResultadoBase(resultadosTemporales);
        
        // 3. Actualizamos la vista
        if (totalUnidad != null) {
            fila.setResultadoBase(totalUnidad.toString());
        } else {
            fila.setResultadoBase("0.00");
        }
    }

    // ==========================================
    // GUARDADO EN LOTE
    // ==========================================

    @FXML
    private void handleGuardarCalificaciones() {
        try {
            List<Resultado> loteResultados = new ArrayList<>();
            
            // Recorremos cada fila (Alumno) y cada columna dinámica (Actividad)
            for (FilaCalificacion fila : datosTabla) {
                for (ActividadGrupo actividad : actividadesUnidad) {
                    String califStr = fila.getCalificacionValue(actividad.getId());
                    
                    Resultado r = new Resultado();
                    r.setInscripcionId(fila.getInscripcionId());
                    r.setActividadGrupoId(actividad.getId());
                    
                    if (!califStr.trim().isEmpty()) {
                        r.setCalificacion(new BigDecimal(califStr));
                    } else {
                        r.setCalificacion(null); // Si el maestro borró la celda, guardamos NULL
                    }
                    
                    loteResultados.add(r);
                }
            }
            
            // Usamos tu método transaccional que valida bloqueos (Locking)
            int unidadId = comboUnidades.getValue().getId();
            resultadoService.guardarLote(grupoActual.getId(), unidadId, loteResultados);
            
            mostrarAdvertencia("¡Calificaciones guardadas exitosamente!", false);
            
        } catch (Exception e) {
            mostrarAdvertencia("Error al guardar: " + e.getMessage(), true);
        }
    }

    private void mostrarAdvertencia(String mensaje, boolean esError) {
        lblAdvertencia.setText(mensaje);
        panelAdvertencia.setVisible(true);
        panelAdvertencia.setManaged(true);
        if (esError) {
            panelAdvertencia.setStyle("-fx-background-color: #f8d7da; -fx-border-color: #f5c6cb;");
            lblAdvertencia.setStyle("-fx-text-fill: #721c24;");
        } else {
            panelAdvertencia.setStyle("-fx-background-color: #d4edda; -fx-border-color: #c3e6cb;");
            lblAdvertencia.setStyle("-fx-text-fill: #155724;");
            
            // Auto ocultar éxito
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(3));
            pause.setOnFinished(e -> {
                panelAdvertencia.setVisible(false);
                panelAdvertencia.setManaged(false);
            });
            pause.play();
        }
    }

    // ==========================================
    // CLASE ENVOLTORIO (Wrapper) PARA LA FILA
    // ==========================================
    public static class FilaCalificacion {
        private final Alumno alumno;
        private final int inscripcionId; // <-- NUEVO
        private final Map<Integer, SimpleStringProperty> calificacionesActividades;
        private final SimpleStringProperty resultadoBase;

        public FilaCalificacion(Alumno alumno, int inscripcionId) {
            this.alumno = alumno;
            this.inscripcionId = inscripcionId;
            this.calificacionesActividades = new HashMap<>();
            this.resultadoBase = new SimpleStringProperty("0.00");
        }

        public Alumno getAlumno() { return alumno; }
        public int getInscripcionId() { return inscripcionId; } // <-- NUEVO Getter

        public SimpleStringProperty getCalificacionProperty(int actividadId) {
            return calificacionesActividades.computeIfAbsent(actividadId, k -> new SimpleStringProperty(""));
        }

        public String getCalificacionValue(int actividadId) {
            return getCalificacionProperty(actividadId).get();
        }

        public void setCalificacion(int actividadId, String valor) {
            getCalificacionProperty(actividadId).set(valor);
        }

        public SimpleStringProperty resultadoBaseProperty() { return resultadoBase; }
        public void setResultadoBase(String valor) { this.resultadoBase.set(valor); }
    }
}