package com.academico.service.individuals;

import com.academico.dao.MaestroDAO;
import com.academico.model.Maestro;
import com.academico.service.AuthService;

import java.sql.SQLException;
import java.util.List;

public class MaestroService {
    private final MaestroDAO maestroDAO;
    private final AuthService authService;

    public MaestroService(){
        this.maestroDAO = new MaestroDAO();
        this.authService = new AuthService();
    }

    public MaestroService(MaestroDAO maestroDAO, AuthService authService) {
        this.maestroDAO = maestroDAO;
        this.authService = authService;
    }

    public List<Maestro> listarTodos() throws Exception {
        try { return maestroDAO.findAll(); } 
        catch (SQLException e) { throw new Exception("Error al cargar la lista de maestros."); }
    }

    // Guardar modificado para soportar Creación y Edición
    public void guardar(Maestro maestro, boolean esEdicion) throws Exception {
        if (maestro.getNombre().isBlank() || maestro.getNumEmpleado().isBlank()) {
            throw new IllegalArgumentException("El nombre y número de empleado son obligatorios.");
        }
        if (maestro.getEmail() != null && !maestro.getEmail().isBlank()) {
            if (!maestro.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-z]{2,}$")) {
                throw new IllegalArgumentException("El formato del correo electrónico es inválido.");
            }
        }
        try {
            if (esEdicion) {
                maestroDAO.actualizar(maestro);
            } else {
                String hashSeguro = authService.hashearPassword("123456");

                maestroDAO.crear(maestro, hashSeguro);
            }
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                if (e.getMessage().contains("num_empleado")) throw new Exception("El número de empleado ya existe.");
                if (e.getMessage().contains("email")) throw new Exception("El correo ya está registrado.");
            }
            throw new Exception("Error al registrar al docente.");
        }
    }

    // === NUEVO: Método para cambiar estado (Activar / Desactivar) ===
    public void cambiarEstado(int id, boolean estado) throws Exception {
        try {
            maestroDAO.cambiarEstado(id, estado);
        } catch (SQLException e) {
            throw new Exception("Error al actualizar el acceso del docente.");
        }
    }

    // === NUEVO: Método para eliminar (Hard Delete) ===
    public void eliminar(int id) throws Exception {
        try {
            maestroDAO.eliminar(id);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No se puede eliminar: El docente tiene grupos asignados. Utiliza la opción 'Desactivar'.");
            }
            throw new Exception("Error al eliminar el registro.");
        }
    }
}