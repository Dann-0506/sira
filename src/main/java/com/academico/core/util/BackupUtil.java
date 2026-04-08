package com.academico.core.util;

import java.io.File;
import java.io.IOException;

public class BackupUtil {

    /**
     * Genera un archivo .sql con el respaldo completo de la base de datos.
     * Usa la herramienta 'pg_dump' de PostgreSQL.
     */
    public static boolean crearRespaldo(String host, String puerto, String usuario, 
                                        String password, String nombreBd, String rutaArchivoDestino) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", puerto,
                    "-U", usuario,
                    "-F", "c", // Formato custom de postgres
                    "-f", rutaArchivoDestino,
                    nombreBd
            );

            // Inyectamos la contraseña como variable de entorno para que pg_dump no detenga 
            // el proceso pidiendo teclear la contraseña en la terminal.
            pb.environment().put("PGPASSWORD", password);
            
            Process proceso = pb.start();
            int exitCode = proceso.waitFor();
            
            return exitCode == 0; // Un código de salida 0 indica que el proceso terminó correctamente
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al generar el respaldo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Restaura un archivo de respaldo en la base de datos.
     * Usa la herramienta 'pg_restore' de PostgreSQL.
     */
    public static boolean restaurarRespaldo(String host, String puerto, String usuario, 
                                            String password, String nombreBd, String rutaArchivoOrigen) {
        try {
            // Verificamos que el archivo de origen exista antes de intentar restaurar
            File archivo = new File(rutaArchivoOrigen);
            if (!archivo.exists()) {
                System.err.println("El archivo de respaldo no existe: " + rutaArchivoOrigen);
                return false;
            }

            ProcessBuilder pb = new ProcessBuilder(
                    "pg_restore",
                    "-h", host,
                    "-p", puerto,
                    "-U", usuario,
                    "-d", nombreBd,
                    "-1", // Ejecutar todo en una sola transacción
                    "--clean", // Limpiar (borrar) objetos antes de crearlos
                    rutaArchivoOrigen
            );

            pb.environment().put("PGPASSWORD", password);
            
            Process proceso = pb.start();
            int exitCode = proceso.waitFor();
            
            return exitCode == 0;
            
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al restaurar el respaldo: " + e.getMessage());
            return false;
        }
    }
}