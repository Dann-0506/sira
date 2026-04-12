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
                    String clave = fila[0].trim();
                    String nombre = fila[1].trim();
                    int totalUnidades = Integer.parseInt(fila[2].trim());

                    List<String> nombresUnidades = null;
                    if (fila.length > 3 && fila[3] != null && !fila[3].trim().isEmpty()) {
                        nombresUnidades = java.util.Arrays.asList(fila[3].split("\\|"));
                    }

                    Materia materia = new Materia();
                    materia.setClave(clave);
                    materia.setNombre(nombre);
                    materia.setTotalUnidades(totalUnidades);

                    // Enviamos los nombres al nuevo servicio sobrecargado
                    materiaService.guardar(materia, false, nombresUnidades);
                    
                } catch (NumberFormatException e) {
                    errores.add("Línea " + (i + 1) + ": El total de unidades debe ser un número válido.");
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
            List<String[]> filas = CsvUtil.leerCsv(is);
            for (int i = 0; i < filas.size(); i++) {
                String[] fila = filas.get(i);
                
                if (i == 0 && esEncabezado(fila, "materia", "clave", "docente")) continue;
                
                // Validamos que tenga las 4 columnas: ClaveMateria, NumEmpleado, ClaveGrupo, Semestre
                if (fila.length < 4) {
                    errores.add("Línea " + (i + 1) + ": Columnas insuficientes (Se requiere: Materia, Docente, Clave Grupo, Semestre).");
                    continue;
                }

                try {
                    String claveMateria  = fila[0].trim();
                    String numEmpleado   = fila[1].trim();
                    String claveGrupo    = fila[2].trim();
                    String semestre      = fila[3].trim();

                    Materia materia = materiaService.buscarPorClave(claveMateria);
                    Maestro maestro = maestroService.buscarPorNumEmpleado(numEmpleado);

                    Grupo grupo = new Grupo();
                    grupo.setMateriaId(materia.getId());
                    grupo.setMaestroId(maestro.getId());
                    grupo.setClave(claveGrupo);
                    grupo.setSemestre(semestre);
                    grupo.setActivo(true);

                    // El Service ahora inyecta los límites históricos automáticamente
                    grupoService.guardar(grupo, false);

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
                
                if (i == 0 && esEncabezado(fila, "matricula", "alumno", "grupo")) continue;

                // EXIGIMOS 3 COLUMNAS: Matricula, ClaveGrupo, Semestre
                if (fila.length < 3) {
                    errores.add("Línea " + (i + 1) + ": Faltan datos (Se requiere: Matrícula, Clave de Grupo, Semestre).");
                    continue;
                }

                try {
                    String matricula = fila[0].trim();
                    String claveGrupo = fila[1].trim();
                    String semestre = fila[2].trim(); // <--- NUEVO CAMPO

                    Alumno alumno = alumnoService.buscarPorMatricula(matricula);
                    
                    // USAMOS LA NUEVA BÚSQUEDA COMPUESTA
                    Grupo grupo = grupoService.buscarPorClaveYSemestre(claveGrupo, semestre);

                    Inscripcion ins = new Inscripcion();
                    ins.setAlumnoId(alumno.getId());
                    ins.setGrupoId(grupo.getId());

                    inscripcionService.inscribir(ins);
                    
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