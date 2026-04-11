package com.academico.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilería para la lectura y escritura de archivos CSV.
 * Responsabilidad: Abstraer la complejidad de la librería OpenCSV y 
 * proporcionar métodos limpios para procesar flujos de datos.
 */
public class CsvUtil {

    // ==========================================
    // LECTURA DE ARCHIVOS
    // ==========================================

    /**
     * Lee cualquier archivo CSV genérico y devuelve una lista de arreglos de texto.
     * Cada arreglo representa una fila, y cada elemento del arreglo es una columna.
     */
    public static List<String[]> leerCsv(InputStream is) throws IOException, CsvValidationException {
        List<String[]> registros = new ArrayList<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(is))) {
            String[] linea;
            while ((linea = reader.readNext()) != null) {
                registros.add(linea);
            }
        }
        return registros;
    }

    // ==========================================
    // ESCRITURA DE ARCHIVOS
    // ==========================================

    /**
     * Toma una lista de datos genéricos y los escribe en un archivo CSV.
     */
    public static void escribirCsv(OutputStream os, String[] cabeceras, List<String[]> datos) throws IOException {
        // Se usa el separador por defecto de la librería (coma)
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(os))) {
            if (cabeceras != null && cabeceras.length > 0) {
                writer.writeNext(cabeceras);
            }
            writer.writeAll(datos);
        }
    }
}