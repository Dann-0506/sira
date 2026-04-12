package com.academico.controller;

import com.academico.model.Alumno;
import com.academico.model.Grupo;
import com.academico.service.individuals.GrupoService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import java.util.List;

public class MisCursosAlumnoController {

    @FXML private FlowPane contenedorTarjetas;
    @FXML private Label labelCargando;

    // === SERVICIOS ===
    private final GrupoService grupoService = new GrupoService();
    private Alumno alumnoLogueado;

    @FXML
    public void initialize() {
        if (DashboardAlumnoController.instancia != null) {
            this.alumnoLogueado = DashboardAlumnoController.instancia.getPerfilAlumno();
            cargarCursos();
        }
    }

    private void cargarCursos() {
        try {
            List<Grupo> misCursos = grupoService.buscarGruposPorAlumno(alumnoLogueado.getId());

            if (misCursos.isEmpty()) {
                labelCargando.setText("No te encuentras inscrito en ningún curso actualmente.");
                return;
            }

            labelCargando.setVisible(false);
            labelCargando.setManaged(false);
            contenedorTarjetas.getChildren().clear();

            for (Grupo grupo : misCursos) {
                VBox tarjeta = crearTarjetaCurso(grupo);
                contenedorTarjetas.getChildren().add(tarjeta);
            }

        } catch (Exception e) {
            labelCargando.setText("Error al cargar los cursos: " + e.getMessage());
            labelCargando.setStyle("-fx-text-fill: #cf222e;"); 
        }
    }

    private VBox crearTarjetaCurso(Grupo grupo) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(350); 
        tarjeta.setPrefHeight(160);
        
        String estiloBase = "-fx-background-color: white; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                            "-fx-border-color: #d0d7de; -fx-border-radius: 8;";
        
        String estiloHover = "-fx-background-color: #f6f8fa; -fx-background-radius: 8; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
                             "-fx-border-color: #0969da; -fx-border-radius: 8;";

        tarjeta.setStyle(estiloBase);
        tarjeta.setCursor(Cursor.HAND);

        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloHover));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(estiloBase));

        // --- INFORMACIÓN DE LA TARJETA ---
        Label lblMateria = new Label(grupo.getMateriaNombre() != null ? grupo.getMateriaNombre() : "Materia no definida");
        lblMateria.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #24292f;");
        lblMateria.setWrapText(true);

        Label lblClave = new Label("Clave: " + grupo.getClave());
        lblClave.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        Label lblMaestro = new Label("Docente: " + (grupo.getMaestroNombre() != null ? grupo.getMaestroNombre() : "No asignado"));
        lblMaestro.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        // Empujar los estados hacia el fondo
        Region espaciador = new Region();
        VBox.setVgrow(espaciador, Priority.ALWAYS);

        // Generar los indicadores de estado dinámicos
        HBox contenedorEstados = generarBadgesDeEstado(grupo);

        tarjeta.getChildren().addAll(lblMateria, lblClave, lblMaestro, espaciador, contenedorEstados);

        // --- NAVEGACIÓN ---
        tarjeta.setOnMouseClicked(e -> {
            if (DashboardAlumnoController.instancia != null) {
                DashboardAlumnoController.instancia.activarMenuDeCurso(grupo);
            }
        });

        return tarjeta;
    }

    /**
     * Genera las etiquetas visuales (badges) informativas para el alumno.
     */
    private HBox generarBadgesDeEstado(Grupo grupo) {
        
        // ESTADO DE EVALUACIÓN
        String textoEvaluacion = "En Evaluación";
        String estiloEvaluacion = "-fx-text-fill: #0969da; -fx-background-color: #ddf4ff;"; // Azul

        if (grupo.isCerrado()) {
            textoEvaluacion = "Acta Firmada";
            estiloEvaluacion = "-fx-text-fill: #155724; -fx-background-color: #d4edda;"; // Verde
        }

        Label lblEvaluacion = new Label(textoEvaluacion);
        lblEvaluacion.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 12; " + estiloEvaluacion);

        return new HBox(8, lblEvaluacion);
    }
}