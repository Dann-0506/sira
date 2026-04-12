package com.academico.controller;

import com.academico.model.*;
import com.academico.service.AlumnoBoletaService;
import com.academico.service.AlumnoBoletaService.*;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class AlumnoCursoDetalleController {

    @FXML private Label lblTituloMateria;
    @FXML private Label lblEstadoCurso;
    
    // Tabla
    @FXML private TableView<FilaUnidadDTO> tablaUnidades;
    @FXML private TableColumn<FilaUnidadDTO, String> colUnidad;
    @FXML private TableColumn<FilaUnidadDTO, String> colCalificacion;
    @FXML private TableColumn<FilaUnidadDTO, String> colEstado;
    @FXML private TableColumn<FilaUnidadDTO, FilaUnidadDTO> colAcciones;

    // Resumen Final
    @FXML private Label lblPromedioBase;
    @FXML private Label lblBonus;
    @FXML private Label lblCalificacionFinal;
    @FXML private Label lblEstadoFinal;
    
    // Override
    @FXML private VBox panelOverride;
    @FXML private Label lblJustificacionOverride;

    // === SERVICIO ORQUESTADOR ===
    private final AlumnoBoletaService boletaService = new AlumnoBoletaService();

    private Grupo cursoActual;
    private Alumno alumnoActual;
    private int inscripcionIdActual = -1;

    @FXML
    public void initialize() {
        if (DashboardAlumnoController.instancia != null) {
            this.cursoActual = DashboardAlumnoController.instancia.getCursoSeleccionado();
            this.alumnoActual = DashboardAlumnoController.instancia.getPerfilAlumno();
            
            configurarCabecera();
            configurarColumnas();
            cargarBoleta();
        }
    }

    private void configurarCabecera() {
        lblTituloMateria.setText(cursoActual.getClave() + " - " + cursoActual.getMateriaNombre());
        if (cursoActual.isCerrado()) {
            lblEstadoCurso.setText("CERRADO (ACTA FIRMADA)");
            lblEstadoCurso.setStyle("-fx-text-fill: #cf222e; -fx-background-color: #ffebe9;");
        } else {
            lblEstadoCurso.setText("EN EVALUACIÓN");
            lblEstadoCurso.setStyle("-fx-text-fill: #0969da; -fx-background-color: #ddf4ff;");
        }
    }

    private void configurarColumnas() {
        colUnidad.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().nombreUnidad()));
        colUnidad.setStyle("-fx-alignment: CENTER-LEFT; -fx-font-weight: bold;");

        colCalificacion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().calificacion()));
        colCalificacion.setStyle("-fx-alignment: CENTER; -fx-font-size: 14px;");

        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));
        colEstado.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            { badge.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    badge.setText(item);
                    aplicarEstiloEstado(badge, item);
                    setGraphic(badge); setStyle("-fx-alignment: CENTER;");
                }
            }
        });

        colAcciones.setCellValueFactory(p -> new SimpleObjectProperty<>(p.getValue()));
        colAcciones.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button("Ver Detalles");
            {
                btn.getStyleClass().add("flat");
                btn.setStyle("-fx-text-fill: #0969da; -fx-cursor: hand;");
                btn.setOnAction(e -> mostrarDesglose(getItem()));
            }
            @Override
            protected void updateItem(FilaUnidadDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else {
                    setGraphic(btn); setStyle("-fx-alignment: CENTER;");
                }
            }
        });
    }

    private void cargarBoleta() {
        try {
            // Toda la lógica compleja se la dejamos al Orquestador
            BoletaDTO boleta = boletaService.obtenerBoletaGlobal(cursoActual, alumnoActual);
            this.inscripcionIdActual = boleta.inscripcionId();

            // Llenar Tabla
            tablaUnidades.setItems(FXCollections.observableArrayList(boleta.filasUnidades()));

            // Llenar Resumen
            lblPromedioBase.setText(boleta.promedioBase());
            
            lblBonus.setText(boleta.bonus());
            lblBonus.setStyle(boleta.tieneBonus() ? "-fx-text-fill: #2da44e;" : "-fx-text-fill: #57606a;");
            
            lblCalificacionFinal.setText(boleta.calificacionFinal());
            lblEstadoFinal.setText(boleta.estadoFinal());
            aplicarEstiloEstado(lblEstadoFinal, boleta.estadoFinal());

            if (boleta.esOverride()) {
                lblCalificacionFinal.setText(boleta.calificacionFinal() + " (M)");
                lblJustificacionOverride.setText("Motivo registrado: " + boleta.justificacionOverride());
                panelOverride.setVisible(true);
                panelOverride.setManaged(true);
            }

        } catch (Exception e) {
            System.err.println("Error al cargar la boleta: " + e.getMessage());
        }
    }

    private void mostrarDesglose(FilaUnidadDTO fila) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Desglose de Calificación");
        info.setHeaderText("Detalle de Evaluación\n" + fila.nombreUnidad());
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");

        try {
            // El orquestador nos devuelve las tareas y los bonus ya organizados
            DesgloseUnidadDTO desglose = boletaService.obtenerDesgloseUnidad(inscripcionIdActual, cursoActual.getId(), fila.unidadId());

            if (desglose.actividades().isEmpty()) {
                content.getChildren().add(new Label("No hay actividades registradas en esta unidad."));
            } else {
                for (DetalleActividadDTO act : desglose.actividades()) {
                    Label lblAct = new Label("• " + act.nombre() + " (" + act.ponderacion() + "%): " + act.calificacion());
                    lblAct.setStyle("-fx-font-size: 14px; -fx-text-fill: #24292f;");
                    content.getChildren().add(lblAct);
                }
            }

            if (desglose.tieneBonus()) {
                content.getChildren().add(new Separator());
                Label lblBonus = new Label("Bonus de Unidad Aplicado: " + desglose.puntosBonus());
                lblBonus.setStyle("-fx-font-weight: bold; -fx-text-fill: #2da44e;");
                
                Label lblJust = new Label("Motivo: " + (desglose.justificacionBonus() != null ? desglose.justificacionBonus() : "N/A"));
                lblJust.setStyle("-fx-font-style: italic; -fx-text-fill: #57606a;");
                
                content.getChildren().addAll(lblBonus, lblJust);
            }

        } catch (Exception e) {
            content.getChildren().add(new Label("Error al cargar el desglose: " + e.getMessage()));
        }

        info.getDialogPane().setContent(content);
        info.showAndWait();
    }

    private void aplicarEstiloEstado(Label badge, String estado) {
        if ("APROBADO".equals(estado)) {
            badge.setStyle("-fx-background-color: #dafbe1; -fx-text-fill: #1a7f37; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else if ("REPROBADO".equals(estado)) {
            badge.setStyle("-fx-background-color: #ffebe9; -fx-text-fill: #cf222e; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        } else {
            badge.setStyle("-fx-background-color: #e1e4e8; -fx-text-fill: #57606a; -fx-font-weight: bold; -fx-padding: 4 12; -fx-background-radius: 12;");
        }
    }
}