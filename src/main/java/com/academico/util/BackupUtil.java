package com.academico.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;

/**
 * Utilería para la gestión de respaldos de la base de datos.
 * Responsabilidad: Interactuar con el sistema operativo para invocar
 * las herramientas nativas de PostgreSQL de forma segura.
 */
public class BackupUtil {

    // ==========================================
    // MÉTODOS DE CREACIÓN DE RESPALDO
    // ==========================================

    public static void crearRespaldoAuto(String rutaDestino) throws Exception {
        Dotenv dotenv = Dotenv.load();
        crearRespaldo(
            dotenv.get("DB_HOST"),
            dotenv.get("DB_PORT"),
            dotenv.get("DB_USER"),
            dotenv.get("DB_PASSWORD"),
            dotenv.get("DB_NAME"),
            rutaDestino
        );
    }

    public static void crearRespaldo(String host, String puerto, String usuario, 
                                     String password, String nombreBd, String rutaDestino) throws Exception {
        try {
            if (!comandoExiste("pg_dump")) {
                throw new Exception("Error crítico: 'pg_dump' no está instalado o no está en las variables de entorno (PATH).");
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump", "-h", host, "-p", puerto, "-U", usuario,
                    "-F", "c", "-f", rutaDestino, nombreBd
            );

            pb.environment().put("PGPASSWORD", password);
            Process proceso = pb.start();
            
            int exitCode = proceso.waitFor();
            if (exitCode != 0) {
                throw new Exception("El proceso de PostgreSQL falló con el código de error: " + exitCode);
            }
            
        } catch (IOException | InterruptedException e) {
            throw new Exception("Error inesperado al intentar generar el respaldo: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS DE RESTAURACIÓN DE RESPALDO
    // ==========================================

    public static void restaurarRespaldoAuto(String rutaOrigen) throws Exception {
        Dotenv dotenv = Dotenv.load();
        restaurarRespaldo(
            dotenv.get("DB_HOST"),
            dotenv.get("DB_PORT"),
            dotenv.get("DB_USER"),
            dotenv.get("DB_PASSWORD"),
            dotenv.get("DB_NAME"),
            rutaOrigen
        );
    }

    public static void restaurarRespaldo(String host, String puerto, String usuario, 
                                         String password, String nombreBd, String rutaOrigen) throws Exception {
        try {
            if (!comandoExiste("pg_restore")) {
                throw new Exception("Error crítico: 'pg_restore' no está instalado o no está en las variables de entorno (PATH).");
            }

            // pg_restore con banderas para limpiar (-c) y hacer todo en una transacción (-1)
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_restore", "-h", host, "-p", puerto, "-U", usuario,
                    "-d", nombreBd, "-c", "-1", rutaOrigen
            );

            pb.environment().put("PGPASSWORD", password);
            Process proceso = pb.start();
            
            int exitCode = proceso.waitFor();
            if (exitCode != 0) {
                throw new Exception("El proceso de restauración falló con el código de error: " + exitCode + ". Asegúrate de que el archivo sea compatible.");
            }
            
        } catch (IOException | InterruptedException e) {
            throw new Exception("Error inesperado al intentar restaurar el respaldo: " + e.getMessage());
        }
    }

    // ==========================================
    // VALIDACIONES DEL SISTEMA
    // ==========================================

    private static boolean comandoExiste(String comando) {
        try {
            Process p = new ProcessBuilder(comando, "--version").start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}