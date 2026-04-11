package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.model.Grupo;
import com.academico.model.Inscripcion;
import com.academico.model.Unidad;
import com.academico.model.Bonus;
import com.academico.model.Resultado;
import com.academico.model.ResultadoUnidad;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.UnidadService;
import com.academico.service.individuals.BonusService;
import com.academico.service.CalificacionService;
import com.academico.service.individuals.ResultadoService;
import java.util.ArrayList;

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

import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GrupoBonusController {

    // === SERVICIOS ===
    private final AlumnoService alumnoService = new AlumnoService();
    private final InscripcionService inscripcionService = new InscripcionService();
    private final UnidadService unidadService = new UnidadService();
    private final BonusService bonusService = new BonusService();
    private final ResultadoService resultadoService = new ResultadoService();
    private final CalificacionService calificacionService = new CalificacionService();

    // === ELEMENTOS UI ===
    @FXML private TextField campoBusqueda;
    @FXML private TableView<FilaAlumnoBonus> tablaAlumnos;
    
    @FXML private VBox panelAsignacion;
    @FXML private Label lblNombreSeleccionado;
    @FXML private ComboBox<String> comboTipoAplicacion;
    @FXML private TextField campoPuntos;
    @FXML private TextField campoMotivo;
    @FXML private Label lblMensajeFormulario;

    // === ESTADO ===
    private Grupo grupoActual;
    private FilaAlumnoBonus alumnoSeleccionado;
    
    private final DateTimeFormatter formateadorFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ObservableList<FilaAlumnoBonus> listaDatos = FXCollections.observableArrayList();
    private FilteredList<FilaAlumnoBonus> datosFiltrados;
    private Map<Integer, Integer> mapaInscripciones = new HashMap<>();
    private List<Unidad> unidadesGrupo;

    @FXML
    public void initialize() {
        if (DashboardMaestroController.instancia != null) {
            grupoActual = DashboardMaestroController.instancia.getGrupoSeleccionado();
        }

        cargarDatosIniciales();
    }

    // ==========================================
    // CARGA DE DATOS Y COLUMNAS DINÁMICAS
    // ==========================================

    private void cargarDatosIniciales() {
        try {
            listaDatos.clear();
            unidadesGrupo = unidadService.listarPorMateria(grupoActual.getMateriaId());
            ObservableList<String> opciones = FXCollections.observableArrayList();
            for (Unidad u : unidadesGrupo) {
                opciones.add("Unidad " + u.getNumero() + " (" + u.getNombre() + ")");
            }
            opciones.add("Calificación Final (Materia)");
            comboTipoAplicacion.setItems(opciones);

            List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoActual.getId());
            for (Inscripcion insc : inscripciones) {
                mapaInscripciones.put(insc.getAlumnoId(), insc.getId());
            }

            List<Alumno> alumnos = alumnoService.buscarPorGrupo(grupoActual.getId());
            for (Alumno alumno : alumnos) {
                int inscId = mapaInscripciones.getOrDefault(alumno.getId(), -1);
                FilaAlumnoBonus fila = new FilaAlumnoBonus(alumno, inscId);
                
                List<ResultadoUnidad> unidadesEvaluadas = new ArrayList<>();

                for (Unidad u : unidadesGrupo) {
                    if (inscId != -1) {
                        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscId, u.getId());
                        
                        Bonus bonusUnidad = bonusService.obtenerBonusUnidad(inscId, u.getId()).orElse(null);
                        BigDecimal puntosBonus = bonusUnidad != null ? bonusUnidad.getPuntos() : BigDecimal.ZERO;

                        ResultadoUnidad ru = calificacionService.calcularResultadoUnidad(inscId, u, resultados, puntosBonus);
                        unidadesEvaluadas.add(ru);

                        String textoUnidad = ru.getResultadoFinal() != null ? ru.getResultadoFinal().toString() : "N/A";
                        fila.setPromedioUnidad(u.getId(), textoUnidad);
                    } else {
                        fila.setPromedioUnidad(u.getId(), "N/A");
                    }
                }

                if (inscId != -1) {
                    Bonus bonusMateria = bonusService.obtenerBonusMateria(inscId).orElse(null);
                    BigDecimal puntosBonusMat = bonusMateria != null ? bonusMateria.getPuntos() : BigDecimal.ZERO;

                    BigDecimal promedioBase = calificacionService.calcularPromedioUnidades(unidadesEvaluadas);
                    BigDecimal finalConBonus = calificacionService.aplicarBonusMateria(promedioBase, puntosBonusMat);

                    String textoFinal = finalConBonus != null ? finalConBonus.toString() : "N/A";
                    fila.setPromedioFinal(textoFinal);
                } else {
                    fila.setPromedioFinal("N/A");
                }
                
                listaDatos.add(fila);
            }

            datosFiltrados = new FilteredList<>(listaDatos, p -> true);
            
            construirColumnas();
            
            tablaAlumnos.setItems(datosFiltrados);

        } catch (Exception e) {
            mostrarMensaje("Error al cargar datos: " + e.getMessage(), true);
        }
    }

    private void construirColumnas() {
        tablaAlumnos.getColumns().clear();
        tablaAlumnos.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        // -- COLUMNA: MATRÍCULA --
        TableColumn<FilaAlumnoBonus, String> colMatricula = new TableColumn<>();
        Label lblMatricula = new Label("Matrícula");
        lblMatricula.setMaxWidth(Double.MAX_VALUE);
        lblMatricula.setAlignment(Pos.CENTER);
        colMatricula.setGraphic(lblMatricula);
        
        colMatricula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlumno().getMatricula()));
        colMatricula.setPrefWidth(100); colMatricula.setMinWidth(100); colMatricula.setMaxWidth(100);
        colMatricula.setStyle("-fx-alignment: CENTER;");
        colMatricula.setReorderable(false);
        colMatricula.setResizable(false);

        // -- COLUMNA: NOMBRE --
        TableColumn<FilaAlumnoBonus, String> colNombre = new TableColumn<>("Nombre del Alumno");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlumno().getNombre()));
        colNombre.setReorderable(false);
        colNombre.setResizable(false);

        tablaAlumnos.getColumns().add(colMatricula);
        tablaAlumnos.getColumns().add(colNombre);

        double anchoDinamico = 0;

        // -- COLUMNAS: UNIDADES --
        for (Unidad unidad : unidadesGrupo) {
            TableColumn<FilaAlumnoBonus, String> colUnidad = new TableColumn<>();
            
            Label lblUnidad = new Label("Unidad " + unidad.getNumero());
            lblUnidad.setMaxWidth(Double.MAX_VALUE);
            lblUnidad.setAlignment(Pos.CENTER);
            colUnidad.setGraphic(lblUnidad);
            
            colUnidad.setCellValueFactory(data -> data.getValue().getPromedioUnidadProperty(unidad.getId()));
            colUnidad.setPrefWidth(90); colUnidad.setMinWidth(90); colUnidad.setMaxWidth(90);
            colUnidad.setStyle("-fx-alignment: CENTER;");
            
            colUnidad.setReorderable(false);
            colUnidad.setResizable(false);
            
            tablaAlumnos.getColumns().add(colUnidad);
            anchoDinamico += 90;
        }

        // -- COLUMNA: PROMEDIO FINAL --
        TableColumn<FilaAlumnoBonus, String> colFinal = new TableColumn<>();
        Label lblFinal = new Label("Prom. Final");
        lblFinal.setMaxWidth(Double.MAX_VALUE);
        lblFinal.setAlignment(Pos.CENTER);
        colFinal.setGraphic(lblFinal);
        
        colFinal.setCellValueFactory(data -> data.getValue().promedioFinalProperty());
        colFinal.setPrefWidth(100); colFinal.setMinWidth(100); colFinal.setMaxWidth(100);
        colFinal.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-background-color: #f6f8fa;");
        
        colFinal.setReorderable(false);
        colFinal.setResizable(false);
        
        // -- COLUMNA: ACCIONES --
        TableColumn<FilaAlumnoBonus, FilaAlumnoBonus> colAcciones = new TableColumn<>();
        Label lblAcciones = new Label("Acciones");
        lblAcciones.setMaxWidth(Double.MAX_VALUE);
        lblAcciones.setAlignment(Pos.CENTER);
        colAcciones.setGraphic(lblAcciones);
        
        colAcciones.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnSeleccionar = new Button("Asignar");
            private final Button btnHistorialFila = new Button("Historial");
            private final HBox panelBotones = new HBox(8, btnSeleccionar, btnHistorialFila);

            {
                panelBotones.setStyle("-fx-alignment: CENTER;");

                btnSeleccionar.getStyleClass().addAll("flat", "accent");
                btnSeleccionar.setOnAction(e -> prepararAsignacion(getItem()));

                btnHistorialFila.getStyleClass().addAll("flat");
                btnHistorialFila.setOnAction(e -> mostrarHistorial(getItem()));
            }

            @Override
            protected void updateItem(FilaAlumnoBonus fila, boolean empty) {
                super.updateItem(fila, empty);
                if (empty || fila == null) {
                    setGraphic(null);
                } else {
                    setGraphic(panelBotones);
                }
            }
        });
        colAcciones.setPrefWidth(180); colAcciones.setMinWidth(180); colAcciones.setMaxWidth(180);
        
        colAcciones.setReorderable(false);
        colAcciones.setResizable(false);

        tablaAlumnos.getColumns().add(colFinal);
        tablaAlumnos.getColumns().add(colAcciones);

        double anchoFijo = 100 + anchoDinamico + 100 + 180 + 3; // +3 por los bordes
        colNombre.prefWidthProperty().bind(tablaAlumnos.widthProperty().subtract(anchoFijo));
    }

    // ==========================================
    // LÓGICA DE BÚSQUEDA Y ASIGNACIÓN
    // ==========================================

    @FXML
    private void handleBusquedaAlumno() {
        String texto = campoBusqueda.getText().toLowerCase().trim();
        datosFiltrados.setPredicate(fila -> {
            if (texto.isEmpty()) return true;
            return fila.getAlumno().getNombre().toLowerCase().contains(texto) ||
                   fila.getAlumno().getMatricula().toLowerCase().contains(texto);
        });
    }

    private void prepararAsignacion(FilaAlumnoBonus fila) {
        if (fila.getInscripcionId() == -1) {
            mostrarMensaje("Error: El alumno no tiene una inscripción válida.", true);
            return;
        }

        alumnoSeleccionado = fila;
        lblNombreSeleccionado.setText(fila.getAlumno().getMatricula() + " - " + fila.getAlumno().getNombre());
        
        campoPuntos.clear();
        campoMotivo.clear();
        comboTipoAplicacion.getSelectionModel().clearSelection();
        lblMensajeFormulario.setVisible(false);
        lblMensajeFormulario.setManaged(false);

        panelAsignacion.setVisible(true);
        panelAsignacion.setManaged(true);
    }

    @FXML
    private void handleGuardarPuntos() {
        String seleccionStr = comboTipoAplicacion.getValue();
        String puntosStr = campoPuntos.getText().trim();
        String motivo = campoMotivo.getText().trim();

        if (seleccionStr == null || puntosStr.isEmpty() || motivo.isEmpty()) {
            mostrarMensaje("Por favor, llena todos los campos.", true);
            return;
        }

        try {
            BigDecimal puntos = new BigDecimal(puntosStr);
            
            if (puntos.compareTo(BigDecimal.ZERO) <= 0 || puntos.compareTo(new BigDecimal("20")) > 0) {
                mostrarMensaje("La cantidad debe ser mayor a 0 y máximo 20 puntos.", true);
                return;
            }

            Integer unidadIdDestino = null;
            String tipoBonus = "materia";

            if (!seleccionStr.equals("Calificación Final (Materia)")) {
                int index = comboTipoAplicacion.getSelectionModel().getSelectedIndex();
                unidadIdDestino = unidadesGrupo.get(index).getId();
                tipoBonus = "unidad";
            }

            Bonus bonus = new Bonus();
            bonus.setInscripcionId(alumnoSeleccionado.getInscripcionId());
            bonus.setUnidadId(unidadIdDestino); 
            bonus.setTipo(tipoBonus); 
            bonus.setPuntos(puntos);
            bonus.setJustificacion(motivo);
            
            bonusService.guardar(bonus);

            cargarDatosIniciales();

            mostrarMensaje("¡Puntos extra asignados correctamente a " + alumnoSeleccionado.getAlumno().getNombre() + "!", false);
            
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            pause.setOnFinished(e -> handleCancelar());
            pause.play();

        } catch (NumberFormatException e) {
            mostrarMensaje("Los puntos deben ser un número válido.", true);
        } catch (Exception e) {
            mostrarMensaje("Error al guardar: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCancelar() {
        panelAsignacion.setVisible(false);
        panelAsignacion.setManaged(false);
        alumnoSeleccionado = null;
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        lblMensajeFormulario.setText(mensaje);
        lblMensajeFormulario.setVisible(true);
        lblMensajeFormulario.setManaged(true);
        
        if (esError) {
            lblMensajeFormulario.setStyle("-fx-text-fill: #cf222e; -fx-padding: 10 0 0 0;");
        } else {
            lblMensajeFormulario.setStyle("-fx-text-fill: #155724; -fx-padding: 10 0 0 0;");
        }
    }

    private void mostrarHistorial(FilaAlumnoBonus fila) {
        if (fila == null) return;

        try {
            List<Bonus> historial = bonusService.obtenerHistorial(fila.getInscripcionId());

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Historial de Puntos Extra");
            dialog.setHeaderText("Auditoría de Puntos: " + fila.getAlumno().getNombre());

            // --- CONSTRUIR LA TABLA DEL MODAL ---
            TableView<Bonus> tablaHistorial = new TableView<>();
            tablaHistorial.setPrefWidth(550);
            tablaHistorial.setPrefHeight(250);
            tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

            TableColumn<Bonus, String> colFecha = new TableColumn<>("Fecha");
            colFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getOtorgadoEn() != null ? data.getValue().getOtorgadoEn().format(formateadorFecha) : "N/A"
            ));
            colFecha.setPrefWidth(120);

            TableColumn<Bonus, String> colTipo = new TableColumn<>("Aplicación");
            colTipo.setCellValueFactory(data -> {
                if ("unidad".equals(data.getValue().getTipo())) {
                    int idUnidad = data.getValue().getUnidadId();
                    String numeroUnidad = unidadesGrupo.stream()
                            .filter(u -> u.getId() == idUnidad)
                            .map(u -> String.valueOf(u.getNumero()))
                            .findFirst()
                            .orElse("?"); 
                    return new SimpleStringProperty("Unidad " + numeroUnidad);
                }
                return new SimpleStringProperty("Materia Final");
            });
            colTipo.setPrefWidth(100);

            TableColumn<Bonus, String> colPuntos = new TableColumn<>("Puntos");
            colPuntos.setCellValueFactory(data -> new SimpleStringProperty("+" + data.getValue().getPuntos().toString()));
            colPuntos.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #155724;");
            colPuntos.setPrefWidth(70);

            TableColumn<Bonus, String> colMotivo = new TableColumn<>("Justificación");
            colMotivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getJustificacion()));

            tablaHistorial.getColumns().add(colFecha);
            tablaHistorial.getColumns().add(colTipo);
            tablaHistorial.getColumns().add(colPuntos);
            tablaHistorial.getColumns().add(colMotivo);
            
            if (historial.isEmpty()) {
                tablaHistorial.setPlaceholder(new Label("Este alumno no tiene puntos extra registrados."));
            } else {
                tablaHistorial.getItems().addAll(historial);
            }

            // --- CONFIGURAR Y MOSTRAR MODAL ---
            dialog.getDialogPane().setContent(tablaHistorial);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (Exception e) {
            mostrarMensaje("Error al cargar el historial: " + e.getMessage(), true);
        }
    }

    // ==========================================
    // CLASE ENVOLTORIO (Wrapper)
    // ==========================================
    public static class FilaAlumnoBonus {
        private final Alumno alumno;
        private final int inscripcionId;
        private final Map<Integer, SimpleStringProperty> promediosUnidades;
        private final SimpleStringProperty promedioFinal;

        public FilaAlumnoBonus(Alumno alumno, int inscripcionId) {
            this.alumno = alumno;
            this.inscripcionId = inscripcionId;
            this.promediosUnidades = new HashMap<>();
            this.promedioFinal = new SimpleStringProperty("0.00");
        }

        public Alumno getAlumno() { return alumno; }
        public int getInscripcionId() { return inscripcionId; }

        public SimpleStringProperty getPromedioUnidadProperty(int unidadId) {
            return promediosUnidades.computeIfAbsent(unidadId, k -> new SimpleStringProperty("0.00"));
        }
        public void setPromedioUnidad(int unidadId, String valor) {
            getPromedioUnidadProperty(unidadId).set(valor);
        }

        public SimpleStringProperty promedioFinalProperty() { return promedioFinal; }
        public void setPromedioFinal(String valor) { this.promedioFinal.set(valor); }
    }
}