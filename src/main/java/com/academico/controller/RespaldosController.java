package com.academico.controller;

import com.academico.model.Respaldo;
import com.academico.service.individuals.RespaldoService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RespaldosController {

    @FXML private TableView<Respaldo> tablaRespaldos;
    @FXML private TableColumn<Respaldo, String> colNombre, colTamano;
    @FXML private TableColumn<Respaldo, LocalDateTime> colFecha;
    @FXML private TableColumn<Respaldo, Void> colAcciones;

    @FXML private StackPane panelConfirmacion, panelCarga;
    @FXML private Label lblTituloConfirmacion, lblMensajeConfirmacion, mensajeGeneral;
    @FXML private Button btnConfirmarAccion;

    private final RespaldoService respaldoService = new RespaldoService();
    private final ObservableList<Respaldo> listaRespaldos = FXCollections.observableArrayList();
    private Runnable accionPendiente;

    @FXML
    public void initialize() {
        tablaRespaldos.setFixedCellSize(48);
        configurarColumnas();
        cargarDatos();
    }

    private void configurarColumnas() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreArchivo"));
        colTamano.setCellValueFactory(new PropertyValueFactory<>("tamanoMegabytes"));
        
        // Formatear Fecha
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaCreacion"));
        colFecha.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            @Override
            protected void updateItem(LocalDateTime date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Botones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnRestaurar = new Button("Restaurar DB");
            private final Button btnEliminar = new Button("Eliminar Archivo");
            private final HBox panel = new HBox(10, btnRestaurar, btnEliminar);

            {
                btnRestaurar.getStyleClass().addAll("warning", "flat");
                btnEliminar.getStyleClass().addAll("danger", "flat");
                panel.setStyle("-fx-alignment: center;");

                btnRestaurar.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        confirmarRestauracion((Respaldo) getTableRow().getItem());
                    }
                });

                btnEliminar.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        confirmarEliminacion((Respaldo) getTableRow().getItem());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    setGraphic(panel);
                }
            }
        });
    }

    private void cargarDatos() {
        try {
            List<Respaldo> respaldos = respaldoService.listarRespaldos();
            listaRespaldos.setAll(respaldos);
            tablaRespaldos.setItems(listaRespaldos);
            tablaRespaldos.refresh();
        } catch (Exception e) {
            mostrarNotificacion("Error al leer la carpeta de respaldos: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleCrearRespaldo() {
        panelCarga.setVisible(true);
        panelCarga.setManaged(true);

        // Hilo secundario para no congelar la UI
        new Thread(() -> {
            try {
                respaldoService.crearRespaldo();
                Platform.runLater(() -> {
                    mostrarNotificacion("¡Respaldo de base de datos generado exitosamente!", false);
                    cargarDatos();
                });
            } catch (Exception e) {
                Platform.runLater(() -> mostrarNotificacion(e.getMessage(), true));
            } finally {
                Platform.runLater(() -> {
                    panelCarga.setVisible(false);
                    panelCarga.setManaged(false);
                });
            }
        }).start();
    }

    private void confirmarRestauracion(Respaldo r) {
        mostrarConfirmacion(
            "¡ADVERTENCIA CRÍTICA!",
            "Estás a punto de RESTAURAR la base de datos usando el archivo: \n" + r.getNombreArchivo() + 
            "\n\nEsto BORRARÁ por completo todos los datos actuales del sistema (Alumnos, Calificaciones, Maestros) y los reemplazará por la información de esta copia de seguridad.\n\n¿Estás absolutamente seguro de querer restaurar la base de datos?",
            "danger",
            () -> {
                panelCarga.setVisible(true);
                panelCarga.setManaged(true);
                
                new Thread(() -> {
                    try {
                        respaldoService.restaurarRespaldo(r.getRutaCompleta());
                        Platform.runLater(() -> {
                            mostrarNotificacion("¡Base de datos restaurada exitosamente!", false);
                            cargarDatos();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> mostrarNotificacion(e.getMessage(), true));
                    } finally {
                        Platform.runLater(() -> {
                            panelCarga.setVisible(false);
                            panelCarga.setManaged(false);
                        });
                    }
                }).start();
            }
        );
    }

    private void confirmarEliminacion(Respaldo r) {
        mostrarConfirmacion(
            "Eliminar Archivo de Respaldo",
            "¿Deseas eliminar físicamente este archivo de respaldo (" + r.getNombreArchivo() + ") de tu disco duro? Esta acción no se puede deshacer.",
            "danger",
            () -> {
                try {
                    respaldoService.eliminarRespaldo(r.getRutaCompleta());
                    cargarDatos();
                    mostrarNotificacion("Archivo de respaldo eliminado.", false);
                } catch (Exception e) {
                    mostrarNotificacion(e.getMessage(), true);
                }
            }
        );
    }

    // === UTILIDADES VISUALES ===
    private void mostrarConfirmacion(String tit, String msj, String clase, Runnable acc) {
        lblTituloConfirmacion.setText(tit);
        lblMensajeConfirmacion.setText(msj);
        btnConfirmarAccion.getStyleClass().setAll("button", clase);
        this.accionPendiente = acc;
        panelConfirmacion.setVisible(true);
        panelConfirmacion.setManaged(true);
    }

    @FXML private void handleCancelarConfirmacion() { panelConfirmacion.setVisible(false); panelConfirmacion.setManaged(false); }
    @FXML private void handleEjecutarConfirmacion() { if (accionPendiente != null) accionPendiente.run(); handleCancelarConfirmacion(); }

    private void mostrarNotificacion(String msj, boolean error) {
        mensajeGeneral.setText(msj);
        mensajeGeneral.setVisible(true); 
        mensajeGeneral.setManaged(true);
        mensajeGeneral.setStyle("-fx-background-color: " + (error ? "#f8d7da" : "#d4edda") + 
                                "; -fx-text-fill: " + (error ? "#721c24" : "#155724") + 
                                "; -fx-padding: 12 25; -fx-background-radius: 30; -fx-font-weight: bold;");
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(Duration.seconds(1), mensajeGeneral);
        fade.setDelay(Duration.seconds(3)); fade.setFromValue(1.0); fade.setToValue(0.0);
        fade.setOnFinished(e -> { mensajeGeneral.setVisible(false); mensajeGeneral.setManaged(false); });
        fade.play();
    }
}