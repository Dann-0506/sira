package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.GrupoService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.MaestroService;
import com.academico.service.individuals.MateriaService;
import com.academico.util.CsvUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador de Carga Masiva. 
 * Responsabilidad: Parsear archivos CSV, validar estructura base y delegar 
 * las reglas de negocio a los servicios individuales de cada entidad.
 */
public class CargaDatosService {

    // === DEPENDENCIAS DE SERVICIOS ===
    private final AlumnoService alumnoService;
    private final MateriaService materiaService;
    private final MaestroService maestroService;
    private final GrupoService grupoService;
    private final InscripcionService inscripcionService;

    // ==========================================
    // CONSTRUCTORES (Inyección de dependencias)
    // ==========================================

    public CargaDatosService() {
        this.alumnoService = new AlumnoService();
        this.materiaService = new MateriaService();
        this.maestroService = new MaestroService();
        this.grupoService = new GrupoService();
        this.inscripcionService = new InscripcionService();
    }

    public CargaDatosService(AlumnoService alumnoService, MateriaService materiaService, 
                             MaestroService maestroService, GrupoService grupoService, 
                             InscripcionService inscripcionService) {
        this.alumnoService = alumnoService;
        this.materiaService = materiaService;
        this.maestroService = maestroService;
        this.grupoService = grupoService;
        this.inscripcionService = inscripcionService;
    }

    // ==========================================
    // MÉTODOS DE IMPORTACIÓN MASIVA
    // ==========================================

    public List<String> importarAlumnosCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);

                if (i == 0 && esEncabezado(fila, "matricula", "mat", "alumno")) continue;

                if (fila.length < 2) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias (Matrícula, Nombre).");
                    continue;
                }

                try {
                    Alumno a = new Alumno();
                    a.setMatricula(fila[0].trim());
                    a.setNombre(fila[1].trim());
                    if (fila.length >= 3 && !fila[2].trim().isEmpty()) {
                        a.setEmail(fila[2].trim());
                    }
                    
                    alumnoService.guardar(a, false); 
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al procesar el archivo de alumnos.");
        }
        return errores;
    }

    public List<String> importarMateriasCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                
                if (i == 0 && esEncabezado(fila, "clave", "materia")) continue;

                if (fila.length < 3) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias (Clave, Nombre, Unidades).");
                    continue;
                }

                try {
                    Materia m = new Materia();
                    m.setClave(fila[0].trim());
                    m.setNombre(fila[1].trim());
                    m.setTotalUnidades(Integer.parseInt(fila[2].trim()));

                    materiaService.guardar(m, false); 
                } catch (NumberFormatException e) {
                    errores.add("Línea " + (i + 1) + ": El total de unidades debe ser un número entero.");
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al procesar el archivo de materias.");
        }
        return errores;
    }

    public List<String> importarMaestrosCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                
                if (i == 0 && esEncabezado(fila, "num", "empleado", "id")) continue;

                if (fila.length < 2) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias (Num Empleado, Nombre).");
                    continue;
                }

                try {
                    Maestro m = new Maestro();
                    m.setNumEmpleado(fila[0].trim());
                    m.setNombre(fila[1].trim());
                    if (fila.length >= 3 && !fila[2].trim().isEmpty()) {
                        m.setEmail(fila[2].trim());
                    }

                    maestroService.guardar(m, false);
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al procesar el archivo de docentes.");
        }
        return errores;
    }

    public List<String> importarGruposCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                
                if (i == 0 && esEncabezado(fila, "materia", "id")) continue;

                if (fila.length < 4) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias.");
                    continue;
                }

                try {
                    Grupo g = new Grupo();
                    g.setMateriaId(Integer.parseInt(fila[0].trim()));
                    g.setMaestroId(Integer.parseInt(fila[1].trim()));
                    g.setClave(fila[2].trim());
                    g.setSemestre(fila[3].trim());
                    g.setActivo(true);

                    grupoService.guardar(g);
                } catch (NumberFormatException e) {
                    errores.add("Línea " + (i + 1) + ": Los IDs de materia y docente deben ser números.");
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al procesar el archivo de grupos.");
        }
        return errores;
    }

    public List<String> importarInscripcionesCsv(InputStream is) {
        List<String> errores = new ArrayList<>();
        try {
            List<String[]> lineas = CsvUtil.leerCsv(is);
            for (int i = 0; i < lineas.size(); i++) {
                String[] fila = lineas.get(i);
                
                if (i == 0 && esEncabezado(fila, "alumno", "id")) continue;

                if (fila.length < 2) {
                    errores.add("Línea " + (i + 1) + ": Faltan columnas obligatorias.");
                    continue;
                }

                try {
                    Inscripcion ins = new Inscripcion();
                    ins.setAlumnoId(Integer.parseInt(fila[0].trim()));
                    ins.setGrupoId(Integer.parseInt(fila[1].trim()));

                    inscripcionService.inscribir(ins);
                } catch (NumberFormatException e) {
                    errores.add("Línea " + (i + 1) + ": Los IDs de alumno y grupo deben ser números.");
                } catch (Exception e) {
                    errores.add("Línea " + (i + 1) + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errores.add("Error crítico al procesar el archivo de inscripciones.");
        }
        return errores;
    }

    // ==========================================
    // MÉTODOS UTILITARIOS
    // ==========================================

    /**
     * Verifica si una fila de CSV es probablemente un encabezado analizando su primera celda.
     */
    private boolean esEncabezado(String[] fila, String... palabrasClave) {
        if (fila == null || fila.length == 0 || fila[0] == null) return false;
        
        String primeraCelda = fila[0].trim().toLowerCase();
        for (String palabra : palabrasClave) {
            if (primeraCelda.contains(palabra.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}