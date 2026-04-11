package com.academico.service;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;
import com.academico.service.individuals.EstadoUnidadService;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Servicio encargado de la gestión de Actividades (Estructura de evaluación).
 * Valida reglas de negocio como el límite del 100% por unidad y el bloqueo de edición.
 */
public class EstructuraAcademicaService {

    // === DEPENDENCIAS ===
    private final ActividadGrupoDAO actividadDAO;
    private final EstadoUnidadService estadoUnidadService;

    // === CONSTRUCTORES ===
    public EstructuraAcademicaService() {
        this.actividadDAO = new ActividadGrupoDAO();
        this.estadoUnidadService = new EstadoUnidadService();
    }

    public EstructuraAcademicaService(ActividadGrupoDAO actividadDAO, EstadoUnidadService estadoUnidadService) {
        this.actividadDAO = actividadDAO;
        this.estadoUnidadService = estadoUnidadService;
    }

    // ==========================================
    // OPERACIONES PRINCIPALES (CRUD)
    // ==========================================

    public ActividadGrupo guardarActividad(ActividadGrupo actividad) throws Exception {
        try {
            // 1. Validar que la unidad permita modificaciones (No esté cerrada)
            estadoUnidadService.validarUnidadAbierta(actividad.getGrupoId(), actividad.getUnidadId());

            // 2. Validar regla de negocio: Ponderación máxima 100%
            if (!puedeAgregarPonderacion(actividad.getGrupoId(), actividad.getUnidadId(), actividad.getPonderacion())) {
                throw new Exception("Error: La suma de las ponderaciones de esta unidad excedería el 100%.");
            }

            // 3. Persistir en base de datos
            return actividadDAO.insertar(actividad);
            
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar guardar la actividad.");
        }
    }

    public ActividadGrupo actualizarActividad(ActividadGrupo actividad) throws Exception {
        try {
            // 1. Validar estado de la unidad
            estadoUnidadService.validarUnidadAbierta(actividad.getGrupoId(), actividad.getUnidadId());

            // 2. Obtener actividad original para calcular la diferencia de puntos
            ActividadGrupo actual = actividadDAO.findById(actividad.getId())
                    .orElseThrow(() -> new Exception("La actividad que intentas editar ya no existe."));
            
            // 3. Lógica Matemática: (Suma Total - Ponderacion Vieja) + Nueva Ponderacion <= 100
            BigDecimal sumaActual = actividadDAO.sumaPonderaciones(actividad.getGrupoId(), actividad.getUnidadId());
            BigDecimal sumaExcluyendoActual = sumaActual.subtract(actual.getPonderacion());
            BigDecimal sumaSimulada = sumaExcluyendoActual.add(actividad.getPonderacion());
            
            if (sumaSimulada.compareTo(new BigDecimal("100.00")) > 0) {
                throw new Exception("Error: Al actualizar, la suma de ponderaciones excedería el 100%.");
            }
            
            // 4. Persistir cambios
            actividadDAO.actualizar(actividad);
            return actividad;
            
        } catch (SQLException e) {
            throw new Exception("Error de conexión al intentar actualizar la actividad.");
        }
    }

    public void eliminarActividad(int actividadId) throws Exception {
        try {
            ActividadGrupo actual = actividadDAO.findById(actividadId)
                 .orElseThrow(() -> new Exception("La actividad no fue encontrada."));
            
            estadoUnidadService.validarUnidadAbierta(actual.getGrupoId(), actual.getUnidadId());
            
            actividadDAO.eliminar(actividadId);
            
        } catch (SQLException e) {
            // Si la actividad ya tiene calificaciones de los alumnos, PostgreSQL lanzará este error (Foreign Key)
            if ("23503".equals(e.getSQLState())) {
                throw new Exception("No puedes eliminar esta actividad porque ya tiene calificaciones registradas. Elimina las calificaciones primero.");
            }
            throw new Exception("Error al intentar eliminar la actividad.");
        }
    }

    // ==========================================
    // VALIDACIONES INTERNAS
    // ==========================================

    /**
     * Comprueba matemáticamente si al sumar una nueva ponderación al acumulado actual,
     * el total se mantiene menor o igual a 100.00.
     */
    private boolean puedeAgregarPonderacion(int grupoId, int unidadId, BigDecimal nuevaPonderacion) throws SQLException {
        BigDecimal sumaActual = actividadDAO.sumaPonderaciones(grupoId, unidadId);
        BigDecimal sumaSimulada = sumaActual.add(nuevaPonderacion);
        
        return sumaSimulada.compareTo(new BigDecimal("100.00")) <= 0;
    }
}