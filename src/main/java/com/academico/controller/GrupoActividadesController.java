package com.academico.controller;

import com.academico.model.ActividadGrupo;
import com.academico.model.Grupo;
import com.academico.model.Unidad;
import com.academico.service.CalificacionService;
import com.academico.service.individuals.ActividadGrupoService;
import com.academico.service.individuals.ResultadoService;
import com.academico.service.individuals.UnidadService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.math.BigDecimal;
import java.util.List;

public class GrupoActividadesController {

    // === SERVICIOS ===
    private final UnidadService unidadService = new UnidadService();
    private final ActividadGrupoService actividadService = new ActividadGrupoService();
    private final ResultadoService resultadoService = new ResultadoService();
    private final CalificacionService calificacionService = new CalificacionService();

    // === ELEMENTOS UI ===
    @FXML private ComboBox<Unidad> comboUnidades;
    @FXML private HBox panelCaptura;
    @FXML private TextField campoNombreActividad;
    @FXML private TextField campoPonderacion;
    @FXML private Button btnGuardarActividad;
    @FXML private Button btnCancelarEdicion;
    
    @FXML private TableView<ActividadGrupo> tablaActividades;
    @FXML private TableColumn<ActividadGrupo, String> colNombre;
    @FXML private TableColumn<ActividadGrupo, BigDecimal> colPonderacion;
    @FXML private TableColumn<ActividadGrupo, ActividadGrupo> colAcciones;
    
    @FXML private HBox panelEstado;
    @FXML private Label labelTotal;
    @FXML private Label labelMensajeValidacion;

    // === MODAL CONFIRMACIÓN ===
    @FXML private StackPane panelConfirmacion;
    @FXML private Label lblTituloConfirmacion;
    @FXML private Label lblMensajeConfirmacion;
    @FXML private Button btnConfirmarAccion;
    private Runnable accionPendiente;

    // === ESTADO ===
    private Grupo grupoActual;
    private ActividadGrupo actividadEnEdicion = null;
    private ObservableList<ActividadGrupo> listaActividades = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        if (DashboardMaestroController.instancia != null) {
            grupoActual = DashboardMaestroController.instancia.getGrupoSeleccionado();
        }

        configurarTabla();
        cargarUnidades();

        comboUnidades.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                panelCaptura.setDisable(false);
                handleCancelarEdicion(); // Limpiar si cambiamos de unidad
                cargarActividades(newVal.getId());
            } else {
                panelCaptura.setDisable(true);
            }
        });
    }

    private void configurarTabla() {
        tablaActividades.setItems(listaActividades);
        
        tablaActividades.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colPonderacion.setMinWidth(150);
        colPonderacion.setMaxWidth(150);
        colPonderacion.setPrefWidth(150);
        
        colAcciones.setMinWidth(180);
        colAcciones.setMaxWidth(180);
        colAcciones.setPrefWidth(180);

        colNombre.prefWidthProperty().bind(
            tablaActividades.widthProperty()
            .subtract(colPonderacion.widthProperty())
            .subtract(colAcciones.widthProperty())
            .subtract(3) 
        );
        // =======================================

        colAcciones.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox container = new HBox(8, btnEditar, btnEliminar);

            {
                btnEditar.getStyleClass().addAll("flat", "accent");
                btnEliminar.getStyleClass().addAll("flat", "danger");
                container.setAlignment(javafx.geometry.Pos.CENTER);

                btnEditar.setOnAction(e -> prepararEdicion(getItem()));
                btnEliminar.setOnAction(e -> solicitarEliminacion(getItem()));
            }

            @Override
            protected void updateItem(ActividadGrupo actividad, boolean empty) {
                super.updateItem(actividad, empty);
                if (empty || actividad == null) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void cargarUnidades() {
        if (grupoActual == null) return;
        try {
            List<Unidad> unidades = unidadService.listarPorMateria(grupoActual.getMateriaId());
            comboUnidades.setItems(FXCollections.observableArrayList(unidades));
        } catch (Exception e) {
            mostrarErrorValidacion("Error al cargar unidades: " + e.getMessage());
        }
    }

    private void cargarActividades(int unidadId) {
        try {
            List<ActividadGrupo> bdActividades = actividadService.buscarPorGrupoYUnidad(grupoActual.getId(), unidadId);
            listaActividades.setAll(bdActividades);
            actualizarBarraDeEstado();
        } catch (Exception e) {
            mostrarErrorValidacion("Error al cargar las actividades.");
        }
    }

    // ==========================================
    // LÓGICA DE GUARDADO (CREAR / EDITAR)
    // ==========================================

    @FXML
    private void handleGuardarActividad() {
        Unidad unidadSel = comboUnidades.getValue();
        String nombre = campoNombreActividad.getText().trim();
        String pesoTexto = campoPonderacion.getText().trim();

        if (unidadSel == null || nombre.isEmpty() || pesoTexto.isEmpty()) {
            mostrarErrorValidacion("Por favor, llena todos los campos.");
            return;
        }

        try {
            BigDecimal nuevoPeso = new BigDecimal(pesoTexto);
            if (nuevoPeso.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarErrorValidacion("La ponderación debe ser mayor a 0.");
                return;
            }

            // Validar matemáticas: Restamos el peso original si estamos editando para calcular el espacio real disponible
            BigDecimal sumaActual = calificacionService.sumarPonderaciones(listaActividades);
            if (actividadEnEdicion != null) {
                sumaActual = sumaActual.subtract(actividadEnEdicion.getPonderacion());
            }
            
            BigDecimal sumaProyectada = sumaActual.add(nuevoPeso);
            if (sumaProyectada.compareTo(new BigDecimal("100.00")) > 0) {
                BigDecimal disponible = new BigDecimal("100.00").subtract(sumaActual);
                mostrarErrorValidacion("¡Error! Solo tienes " + disponible + "% disponible.");
                return;
            }

            // Guardar o Actualizar
            if (actividadEnEdicion == null) {
                actividadEnEdicion = new ActividadGrupo();
                actividadEnEdicion.setGrupoId(grupoActual.getId());
                actividadEnEdicion.setUnidadId(unidadSel.getId());
            }

            actividadEnEdicion.setNombre(nombre);
            actividadEnEdicion.setPonderacion(nuevoPeso);

            actividadService.guardar(actividadEnEdicion);
            
            handleCancelarEdicion();
            cargarActividades(unidadSel.getId());

        } catch (NumberFormatException e) {
            mostrarErrorValidacion("La ponderación debe ser un número válido.");
        } catch (Exception e) {
            mostrarErrorValidacion(e.getMessage());
        }
    }

    private void prepararEdicion(ActividadGrupo actividad) {
        actividadEnEdicion = actividad;
        campoNombreActividad.setText(actividad.getNombre());
        campoPonderacion.setText(actividad.getPonderacion().toString());
        
        btnGuardarActividad.setText("Actualizar");
        btnCancelarEdicion.setVisible(true);
        btnCancelarEdicion.setManaged(true);
        
        // Habilitar captura por si estaba bloqueada por tener 100%
        panelCaptura.setDisable(false);
    }

    @FXML
    private void handleCancelarEdicion() {
        actividadEnEdicion = null;
        campoNombreActividad.clear();
        campoPonderacion.clear();
        
        btnGuardarActividad.setText("Agregar a Rúbrica");
        btnCancelarEdicion.setVisible(false);
        btnCancelarEdicion.setManaged(false);
        actualizarBarraDeEstado(); // Para re-bloquear el panel si ya hay 100%
    }

    // ==========================================
    // LÓGICA DE ELIMINACIÓN Y CONFIRMACIÓN
    // ==========================================

    private void solicitarEliminacion(ActividadGrupo actividad) {
        try {
            // Regla de Negocio: Candado de Integridad Académica
            if (resultadoService.tieneCalificacionesRegistradas(actividad.getId())) {
                mostrarErrorValidacion("🔒 Acción denegada: Esta actividad ya tiene alumnos calificados. No se puede eliminar.");
                return;
            }

            mostrarConfirmacion(
                "Eliminar Actividad",
                "¿Estás seguro de que deseas eliminar la actividad '" + actividad.getNombre() + "'? Esta acción modificará la rúbrica.",
                "Eliminar",
                "danger",
                () -> ejecutarEliminacion(actividad)
            );
        } catch (Exception e) {
            mostrarErrorValidacion("Error al verificar el estado de la actividad.");
        }
    }

    private void ejecutarEliminacion(ActividadGrupo actividad) {
        try {
            actividadService.eliminar(actividad.getId());
            cargarActividades(comboUnidades.getValue().getId());
            handleCancelarEdicion();
        } catch (Exception e) {
            mostrarErrorValidacion(e.getMessage());
        }
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

    // ==========================================
    // UTILERÍAS DE UI
    // ==========================================

    private void actualizarBarraDeEstado() {
        BigDecimal total = calificacionService.sumarPonderaciones(listaActividades);
        labelTotal.setText("Total de la rúbrica: " + total + "%");

        if (calificacionService.ponderacionesValidas(listaActividades)) {
            panelEstado.setStyle("-fx-background-color: #d4edda; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #c3e6cb; -fx-border-radius: 8;");
            labelTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #155724;");
            labelMensajeValidacion.setText("✓ Rúbrica completa. Lista para calificar.");
            labelMensajeValidacion.setStyle("-fx-text-fill: #155724;");
            
            // Solo bloqueamos si no estamos editando
            if (actividadEnEdicion == null) panelCaptura.setDisable(true); 
        } else {
            BigDecimal faltante = calificacionService.ponderacionFaltante(listaActividades);
            panelEstado.setStyle("-fx-background-color: #fff3cd; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #ffeeba; -fx-border-radius: 8;");
            labelTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #856404;");
            labelMensajeValidacion.setText("⚠ Atención: Faltan " + faltante + "% para completar la unidad.");
            labelMensajeValidacion.setStyle("-fx-text-fill: #856404;");
            panelCaptura.setDisable(false);
        }
    }

    private void mostrarErrorValidacion(String mensaje) {
        panelEstado.setStyle("-fx-background-color: #f8d7da; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #f5c6cb; -fx-border-radius: 8;");
        labelTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #721c24;");
        labelMensajeValidacion.setText(mensaje);
        labelMensajeValidacion.setStyle("-fx-text-fill: #721c24;");
    }
}