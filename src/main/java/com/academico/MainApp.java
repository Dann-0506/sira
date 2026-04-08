package com.academico;

import com.academico.db.DatabaseManager;

public class MainApp {

    public static void main(String[] args) {
        try {
            DatabaseManager.initialize();
            System.out.println("Sistema iniciado correctamente.");
            // Aquí irá el arranque de JavaFX cuando llegue el momento
        } catch (Exception e) {
            System.err.println("Error al iniciar el sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.close();
        }
    }
}