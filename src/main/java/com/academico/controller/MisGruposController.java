package com.academico.controller;

import com.academico.model.Grupo;
import com.academico.model.Maestro;
import com.academico.model.Usuario;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.MaestroService;
import com.academico.util.SessionManagerUtil;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Controlador para la vista inicial del Maestro.
 * Despliega los grupos asignados en formato de tarjetas responsivas.
 */
public class MisGruposController {

    @FXML private FlowPane contenedorTarjetas;
    @FXML private Label labelCargando;

    private final MaestroService maestroService = new MaestroService();
    private final GrupoService grupoService = new GrupoService(); 
    private final InscripcionService inscripcionService = new InscripcionService();

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

            // Ocultamos el label si hay datos
            labelCargando.setVisible(false);
            labelCargando.setManaged(false);

            // Generamos una tarjeta visual por cada grupo
            for (Grupo grupo : misGrupos) {
                VBox tarjeta = crearTarjetaGrupo(grupo);
                contenedorTarjetas.getChildren().add(tarjeta);
            }

        } catch (Exception e) {
            labelCargando.setText("Error al cargar los grupos: " + e.getMessage());
            labelCargando.setStyle("-fx-text-fill: #cf222e;"); // Color rojo de error
        }
    }

    /**
     * Construye un VBox estilizado que representa un grupo académico.
     */
    private VBox crearTarjetaGrupo(Grupo grupo) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setPrefWidth(280);
        tarjeta.setPrefHeight(150);
        
        // Estilo base de la tarjeta (borde, fondo blanco, sombra ligera)
        String estiloBase = "-fx-background-color: white; -fx-background-radius: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 3); " +
                            "-fx-border-color: #d0d7de; -fx-border-radius: 8;";
        
        // Estilo Hover (cambia el borde a azul y aumenta la sombra para dar sensación de clic)
        String estiloHover = "-fx-background-color: #f6f8fa; -fx-background-radius: 8; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 5); " +
                             "-fx-border-color: #0969da; -fx-border-radius: 8;";

        tarjeta.setStyle(estiloBase);
        tarjeta.setCursor(Cursor.HAND); // Cambia el cursor a "manita"

        // Eventos para cambiar el estilo cuando el mouse entra o sale
        tarjeta.setOnMouseEntered(e -> tarjeta.setStyle(estiloHover));
        tarjeta.setOnMouseExited(e -> tarjeta.setStyle(estiloBase));

        // --- INFORMACIÓN DE LA TARJETA ---
        
        Label lblMateria = new Label(grupo.getMateriaNombre() != null ? grupo.getMateriaNombre() : "Materia no definida");
        lblMateria.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #24292f;");
        lblMateria.setWrapText(true);

        Label lblClave = new Label("Clave: " + grupo.getClave());
        lblClave.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        // TODO: Si tienes un método para contar inscripciones, úsalo aquí
        int totalAlumnos = 0;
        try {
            totalAlumnos = inscripcionService.contarPorGrupo(grupo.getId());
        } catch (Exception ex) {
            System.err.println("No se pudieron contar los alumnos del grupo " + grupo.getClave());
        }
        
        Label lblAlumnos = new Label("Alumnos inscritos: " + totalAlumnos); 
        lblAlumnos.setStyle("-fx-font-size: 13px; -fx-text-fill: #57606a;");

        // Emular un "Badge" o etiqueta de estado
        Label lblEstado = new Label("Configuración Pendiente");
        // TODO: Aquí podrías añadir lógica para cambiar el color del estado si ya está configurado (Ej: Verde)
        lblEstado.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #9a6700; " +
                           "-fx-background-color: #fff8c5; -fx-padding: 4 8 4 8; -fx-background-radius: 12;");

        // Empujar el estado hacia el fondo de la tarjeta
        javafx.scene.layout.Region espaciador = new javafx.scene.layout.Region();
        VBox.setVgrow(espaciador, javafx.scene.layout.Priority.ALWAYS);

        tarjeta.getChildren().addAll(lblMateria, lblClave, lblAlumnos, espaciador, lblEstado);

        // --- EL CORAZÓN DE LA INTERACCIÓN ---
        // Al hacer clic, le decimos al Dashboard principal que active el menú de este grupo
        tarjeta.setOnMouseClicked(e -> {
            if (DashboardMaestroController.instancia != null) {
                DashboardMaestroController.instancia.activarMenuDeGrupo(grupo);
            }
        });

        return tarjeta;
    }
}
