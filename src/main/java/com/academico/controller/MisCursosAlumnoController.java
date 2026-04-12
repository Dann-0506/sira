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
            // 1. Obtener la lista de grupos donde el alumno está inscrito
            List<Grupo> misCursos = grupoService.buscarGruposPorAlumno(alumnoLogueado.getId());

            if (misCursos.isEmpty()) {
                labelCargando.setText("No te encuentras inscrito en ningún curso actualmente.");
                return;
            }

            // 2. ORDENAMIENTO CRONOLÓGICO: Semestres más recientes primero
            misCursos.sort((g1, g2) -> g2.getSemestre().compareTo(g1.getSemestre()));

            labelCargando.setVisible(false);
            labelCargando.setManaged(false);
            contenedorTarjetas.getChildren().clear();

            // 3. Renderizar las tarjetas
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
        VBox tarjeta = new VBox(10);
        tarjeta.setPrefSize(280, 180);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setCursor(Cursor.HAND);
        tarjeta.getStyleClass().add("card");
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #d0d7de; -fx-border-radius: 12;");

        // Nombre de la Materia
        Label lblMateria = new Label(grupo.getMateriaNombre());
        lblMateria.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #24292f;");
        lblMateria.setWrapText(true);

        // Clave del Grupo y Semestre (Crucial para distinguir repeticiones)
        Label lblInfo = new Label(grupo.getClave() + " | " + grupo.getSemestre());
        lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        Label lblMaestro = new Label("Docente: " + (grupo.getMaestroNombre() != null ? grupo.getMaestroNombre() : "Pendiente"));
        lblMaestro.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        Region espaciador = new Region();
        VBox.setVgrow(espaciador, Priority.ALWAYS);

        // Generar los indicadores de estado dinámicos
        HBox contenedorEstados = generarBadgesDeEstado(grupo);

        tarjeta.getChildren().addAll(lblMateria, lblInfo, lblMaestro, espaciador, contenedorEstados);

        // Navegación al detalle del curso
        tarjeta.setOnMouseClicked(e -> {
            if (DashboardAlumnoController.instancia != null) {
                DashboardAlumnoController.instancia.activarMenuDeCurso(grupo);
            }
        });

        return tarjeta;
    }

    private HBox generarBadgesDeEstado(Grupo grupo) {
        HBox hbox = new HBox(8);
        
        // BADGE 1: ESTADO DEL CURSO (ABIERTO/CERRADO)
        String textoEvaluacion = grupo.isCerrado() ? "Acta Firmada" : "En Evaluación";
        String estiloEvaluacion = grupo.isCerrado() 
            ? "-fx-text-fill: #155724; -fx-background-color: #d4edda;" 
            : "-fx-text-fill: #0969da; -fx-background-color: #ddf4ff;";

        Label lblEvaluacion = new Label(textoEvaluacion);
        lblEvaluacion.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 2 8; -fx-background-radius: 10; " + estiloEvaluacion);

        hbox.getChildren().add(lblEvaluacion);
        return hbox;
    }
}