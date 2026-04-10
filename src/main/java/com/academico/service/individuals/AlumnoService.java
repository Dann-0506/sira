package com.academico.service.individuals;

import com.academico.dao.AlumnoDAO;
import com.academico.model.Alumno;
import java.sql.SQLException;
import java.util.List;

public class AlumnoService {
    private final AlumnoDAO alumnoDAO;

    public AlumnoService() {
        this.alumnoDAO = new AlumnoDAO();
    }

    public AlumnoService(AlumnoDAO alumnoDAO) {
        this.alumnoDAO = alumnoDAO;
    }

    public List<Alumno> listarTodos() throws Exception {
        try {
            return alumnoDAO.obtenerTodos();
        } catch (SQLException e) {
            throw new Exception("No se pudieron cargar los alumnos de la base de datos.");
        }
    }

    public void guardar(Alumno alumno, boolean esEdicion) throws Exception {
        // Validación de negocio previa
        if (alumno.getMatricula().isBlank() || alumno.getNombre().isBlank()) {
            throw new IllegalArgumentException("Campos obligatorios faltantes.");
        }

        try {
            if (esEdicion) {
                alumnoDAO.actualizar(alumno);
            } else {
                alumnoDAO.crear(alumno);
            }
        } catch (SQLException e) {
            // TRADUCCIÓN DE ERRORES DE POSTGRES
            String state = e.getSQLState();
            if ("23505".equals(state)) { // Unique violation
                if (e.getMessage().contains("matricula")) throw new Exception("La matrícula ya existe.");
                if (e.getMessage().contains("email")) throw new Exception("El correo ya está registrado.");
            }
            throw new Exception("Error al procesar la solicitud en la base de datos.");
        }
    }

    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            alumnoDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al actualizar el acceso del alumno.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            alumnoDAO.eliminar(id);
        } catch (SQLException e) {
            // Manejo de Integridad Referencial
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El alumno tiene registros académicos vinculados. Utiliza la opción 'Desactivar'.");
            }
            throw new Exception("Error al eliminar el registro.");
        }
    }

    public Alumno buscarPorId(int id) throws Exception {
        try {
            return alumnoDAO.findById(id).orElse(new Alumno());
        } catch (SQLException e) {
            throw new Exception("Error al buscar los datos del alumno.");
        }
    }
}