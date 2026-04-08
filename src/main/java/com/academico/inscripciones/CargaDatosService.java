package com.academico.inscripciones;

import com.academico.academia.Grupo;
import com.academico.academia.GrupoDAO;
import com.academico.academia.Materia;
import com.academico.academia.MateriaDAO;
import com.academico.core.util.CsvUtil;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CargaDatosService {

    private final AlumnoDAO alumnoDAO;
    private final MateriaDAO materiaDAO;
    private final GrupoDAO grupoDAO;
    private final InscripcionDAO inscripcionDAO;

    public CargaDatosService() {
        this.alumnoDAO = new AlumnoDAO();
        this.materiaDAO = new MateriaDAO();
        this.grupoDAO = new GrupoDAO();
        this.inscripcionDAO = new InscripcionDAO();
    }

    public CargaDatosService(AlumnoDAO alumnoDAO, MateriaDAO materiaDAO, GrupoDAO grupoDAO, InscripcionDAO inscripcionDAO) {
        this.alumnoDAO = alumnoDAO;
        this.materiaDAO = materiaDAO;
        this.grupoDAO = grupoDAO;
        this.inscripcionDAO = inscripcionDAO;
    }

    public List<String> importarAlumnosCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Alumno> alumnos = new ArrayList<>();

        for (String[] fila : lineas) {
            if (fila.length >= 2) {
                Alumno alumno = new Alumno();
                alumno.setMatricula(fila[0].trim());
                alumno.setNombre(fila[1].trim());
                
                if (fila.length >= 3 && !fila[2].trim().isEmpty()) {
                    alumno.setEmail(fila[2].trim());
                }
                
                alumnos.add(alumno);
            }
        }
        return alumnoDAO.insertarLote(alumnos); // Lista de matrículas ya existentes que no se insertaron
    }

    public List<String> importarMateriasCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Materia> materias = new ArrayList<>();

        for (String[] fila : lineas) {
            if (fila.length >= 3) {
                Materia materia = new Materia();
                materia.setClave(fila[0].trim());
                materia.setNombre(fila[1].trim());
                
                try {
                    materia.setTotalUnidades(Integer.parseInt(fila[2].trim()));
                    materias.add(materia);
                } catch (NumberFormatException e) {
                    System.err.println("Error de formato en total de unidades para la materia: " + materia.getClave());
                }
            }
        }
        return materiaDAO.insertarLote(materias); // Lista de claves que ya existian y fueron ignoradas.
    }

    public List<String> importarGruposCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Grupo> grupos = new ArrayList<>();

        for (String[] fila : lineas) {
            if (fila.length >= 4) {
                try {
                    Grupo grupo = new Grupo();
                    grupo.setMateriaId(Integer.parseInt(fila[0].trim()));
                    grupo.setMaestroId(Integer.parseInt(fila[1].trim()));
                    grupo.setClave(fila[2].trim());
                    grupo.setSemestre(fila[3].trim());
                    grupo.setActivo(true);
                    
                    grupos.add(grupo);
                } catch (NumberFormatException e) {
                    System.err.println("Error de formato numérico en la fila del grupo: " + fila[2]);
                }
            }
        }
        return grupoDAO.insertarLote(grupos); // Lista de claves de grupo que ya existían y no se insertaron
    }

    public List<String> importarInscripcionesCsv(InputStream is) throws IOException, CsvValidationException, SQLException {
        List<String[]> lineas = CsvUtil.leerCsv(is);
        List<Inscripcion> inscripciones = new ArrayList<>();

        for (String[] fila : lineas) {
            if (fila.length >= 2) {
                try {
                    Inscripcion inscripcion = new Inscripcion();
                    inscripcion.setAlumnoId(Integer.parseInt(fila[0].trim()));
                    inscripcion.setGrupoId(Integer.parseInt(fila[1].trim()));
                    
                    inscripciones.add(inscripcion);
                } catch (NumberFormatException e) {
                    System.err.println("Error de formato numérico en la inscripción.");
                }
            }
        }
        return inscripcionDAO.insertarLote(inscripciones); // Lista de duplicados que fueron ignoradas
    }
}