package com.academico.service.individuals;

import com.academico.dao.BonusDAO;
import com.academico.model.Bonus;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Servicio CRUD para la gestión de Puntos Extra (Bonus).
 * Responsabilidad: Consultar y asignar puntos adicionales a nivel de unidad o materia completa.
 */
public class BonusService {
    
    // === DEPENDENCIAS ===
    private final BonusDAO bonusDAO;

    // === CONSTRUCTORES ===
    public BonusService() {
        this.bonusDAO = new BonusDAO();
    }

    public BonusService(BonusDAO bonusDAO) {
        this.bonusDAO = bonusDAO;
    }

    // ==========================================
    // OPERACIONES DE LECTURA
    // ==========================================

    public Optional<Bonus> obtenerBonusUnidad(int inscripcionId, int unidadId) throws Exception {
        if (inscripcionId <= 0 || unidadId <= 0) return Optional.empty();
        
        try {
            return bonusDAO.findByInscripcionYUnidad(inscripcionId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar los puntos extra de la unidad.");
        }
    }

    public Optional<Bonus> obtenerBonusMateria(int inscripcionId) throws Exception {
        if (inscripcionId <= 0) return Optional.empty();

        try {
            return bonusDAO.findBonusMateria(inscripcionId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar los puntos extra de la materia.");
        }
    }

    public List<Bonus> obtenerHistorial(int inscripcionId) throws Exception {
        if (inscripcionId <= 0) return List.of();
        try {
            return bonusDAO.findByInscripcion(inscripcionId);
        } catch (SQLException e) {
            throw new Exception("Error al consultar el historial de puntos extra.");
        }
    }

    // ==========================================
    // OPERACIONES DE ESCRITURA
    // ==========================================

    public void guardar(Bonus bonus) throws Exception {
        // 1. Validaciones de negocio
        if (bonus.getPuntos() == null || bonus.getPuntos().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Los puntos extra deben ser mayores a 0.");
        }
        if (bonus.getJustificacion() == null || bonus.getJustificacion().isBlank()) {
            throw new IllegalArgumentException("Es obligatorio justificar el motivo de los puntos extra.");
        }

        // 2. Persistencia
        try {
            bonusDAO.insertar(bonus);
        } catch (SQLException e) {
            // Manejo de error de llave foránea (por si el alumno o unidad ya no existen)
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("Error: La inscripción o la unidad no son válidas.");
            }
            throw new Exception("Error de conexión al intentar guardar la bonificación.");
        }
    }
}