package com.academico.controller;

import com.academico.model.ActividadGrupo;
import com.academico.model.Grupo;
import com.academico.model.Maestro;
import com.academico.model.Unidad;
import com.academico.model.Usuario;
import com.academico.service.CalificacionService;
import com.academico.service.individuals.ActividadGrupoService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.MaestroService;
import com.academico.service.individuals.UnidadService;
import com.academico.util.SessionManagerUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controlador para la vista inicial del Maestro.
 * Despliega los grupos asignados evaluando dinámicamente su estado de configuración.
 */
public class MisGruposController {

    @FXML private FlowPane contenedorTarjetas;
    @FXML private Label labelCargando;

    // === SERVICIOS ===
    private final MaestroService maestroService = new MaestroService();
    private final GrupoService grupoService = new GrupoService(); 
    private final InscripcionService inscripcionService = new InscripcionService();
    
    // Servicios necesarios para calcular el estado dinámico de la Rúbrica
    private final UnidadService unidadService = new UnidadService();
    private final ActividadGrupoService actividadService = new ActividadGrupoService();
    private final CalificacionService calificacionService = new CalificacionService();

    @FXML
    public void initialize() {
        cargarGrupos();
    }

    private void cargarGrupos() {
        try {
            Usuario usuario = SessionManagerUtil.getUsuarioActual();
            Maestro maestro = maestroService.buscarPorUsuarioId(usuario.getId());

            List<Grupo> misGrupos = grupoService.buscarGruposPorMaestro(maestro.getId());

            if (misGrupos.isEmpty()) {
                labelCargando.setText("No tienes grupos asignados actualmente en este periodo.");
                return;
            }

            labelCargando.setVisible(false);
            labelCargando.setManaged(false);
            contenedorTarjetas.getChildren().clear(); // Limpiar por si se recarga la vista

            for (Grupo grupo : misGrupos) {
                VBox tarjeta = crearTarjetaGrupo(grupo);
                contenedorTarjetas.getChildren().add(tarjeta);
            }

        } catch (Exception e) {
            labelCargando.setText("Error al cargar los grupos: " + e.getMessage());
            labelCargando.setStyle("-fx-text-fill: #cf222e;"); 
        }
    }

    private VBox crearTarjetaGrupo(Grupo grupo) {
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

        // Conteo de Alumnos
        int totalAlumnos = 0;
        try {
            totalAlumnos = inscripcionService.contarPorGrupo(grupo.getId());
        } catch (Exception ex) {
            System.err.println("No se pudieron contar los alumnos");
        }
        Label lblAlumnos = new Label("Alumnos inscritos: " + totalAlumnos); 
        lblAlumnos.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        // Empujar los estados hacia el fondo
        Region espaciador = new Region();
        VBox.setVgrow(espaciador, javafx.scene.layout.Priority.ALWAYS);

        // Generar los indicadores de estado dinámicos
        HBox contenedorEstados = generarBadgesDeEstado(grupo, totalAlumnos);

        tarjeta.getChildren().addAll(lblMateria, lblClave, lblAlumnos, espaciador, contenedorEstados);

        // --- NAVEGACIÓN ---
        tarjeta.setOnMouseClicked(e -> {
            if (DashboardMaestroController.instancia != null) {
                DashboardMaestroController.instancia.activarMenuDeGrupo(grupo);
            }
        });

        return tarjeta;
    }

    /**
     * Calcula los dos estados críticos del grupo y genera sus etiquetas visuales.
     */
    private HBox generarBadgesDeEstado(Grupo grupo, int totalAlumnos) {
        
        // 1. ESTADO DE EVALUACIÓN (Sustituye al estado administrativo)
        String textoEvaluacion = "Evaluación Pendiente";
        String estiloEvaluacion = "-fx-text-fill: #9a6700; -fx-background-color: #fff8c5;"; // Amarillo/Naranja

        if (totalAlumnos == 0) {
            textoEvaluacion = "Sin Alumnos";
            estiloEvaluacion = "-fx-text-fill: #57606a; -fx-background-color: #f6f8fa;"; // Gris
        } 
        // TODO: Cuando construyamos la pantalla 4 (Concentrado Final), 
        // aquí validaremos si el maestro ya "Cerró el Acta" para ponerlo en verde.
        /* else if (calificacionService.isActaCerrada(grupo.getId())) {
            textoEvaluacion = "Acta Cerrada";
            estiloEvaluacion = "-fx-text-fill: #2da44e; -fx-background-color: #dcffe4;"; // Verde
        }
        */

        Label lblEvaluacion = new Label(textoEvaluacion);
        lblEvaluacion.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 12; " + estiloEvaluacion);


        // 2. ESTADO DE LA RÚBRICA (Se mantiene igual)
        String textoRubrica = "Desconocido";
        String estiloRubrica = "-fx-text-fill: #57606a; -fx-background-color: #f6f8fa;";

        try {
            List<Unidad> unidades = unidadService.listarPorMateria(grupo.getMateriaId());
            if (unidades.isEmpty()) {
                textoRubrica = "Sin temario";
            } else {
                int unidadesCompletas = 0;
                for (Unidad u : unidades) {
                    List<ActividadGrupo> actividades = actividadService.buscarPorGrupoYUnidad(grupo.getId(), u.getId());
                    if (calificacionService.ponderacionesValidas(actividades)) {
                        unidadesCompletas++;
                    }
                }

                if (unidadesCompletas == 0) {
                    textoRubrica = "Configuración Pendiente";
                    estiloRubrica = "-fx-text-fill: #cf222e; -fx-background-color: #ffebe9;"; // Rojo claro
                } else if (unidadesCompletas < unidades.size()) {
                    textoRubrica = "Rúbrica (" + unidadesCompletas + "/" + unidades.size() + ")";
                    estiloRubrica = "-fx-text-fill: #0969da; -fx-background-color: #ddf4ff;"; // Azul
                } else {
                    textoRubrica = "Rúbrica Completa";
                    estiloRubrica = "-fx-text-fill: #155724; -fx-background-color: #d4edda;"; // Verde
                }
            }
        } catch (Exception e) {
            textoRubrica = "Error de rúbrica";
        }

        Label lblRubrica = new Label(textoRubrica);
        lblRubrica.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8 4 8; -fx-background-radius: 12; " + estiloRubrica);

        return new HBox(8, lblEvaluacion, lblRubrica);
    }
}