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
    // MÉTODOS DE RESPALDO
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
            // VERIFICACIÓN DE HERRAMIENTA (Prevención de errores)
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