package com.academico.core;

import com.academico.core.db.DatabaseManager;

public class MainApp {

    public static void main(String[] args) {
        try {
            DatabaseManager.initialize();
            System.out.println("Sistema iniciado correctamente.");
        } catch (Exception e) {
            System.err.println("Error al iniciar el sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseManager.close();
        }
    }
}