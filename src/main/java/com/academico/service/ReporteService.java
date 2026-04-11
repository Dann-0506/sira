package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.AlumnoService;
import com.academico.service.individuals.BonusService;
import com.academico.service.individuals.InscripcionService;
import com.academico.service.individuals.ResultadoService;
import com.academico.service.individuals.UnidadService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio Orquestador de Reportes.
 * Responsabilidad: Reunir datos de múltiples servicios para generar 
 * cálculos académicos complejos y vistas consolidadas.
 */
public class ReporteService {

    // === DEPENDENCIAS DE SERVICIOS ===
    private final AlumnoService alumnoService;
    private final InscripcionService inscripcionService;
    private final UnidadService unidadService;
    private final BonusService bonusService;
    private final CalificacionService calificacionService;
    private final ResultadoService resultadoService;

    // ==========================================
    // CONSTRUCTORES
    // ==========================================

    public ReporteService() {
        this.alumnoService = new AlumnoService();
        this.inscripcionService = new InscripcionService();
        this.unidadService = new UnidadService();
        this.bonusService = new BonusService();
        this.calificacionService = new CalificacionService();
        this.resultadoService = new ResultadoService();
    }

    public ReporteService(AlumnoService alumnoService, InscripcionService inscripcionService, UnidadService unidadService, 
                          BonusService bonusService, CalificacionService calificacionService, ResultadoService resultadoService) {
        this.alumnoService = alumnoService;
        this.inscripcionService = inscripcionService;
        this.unidadService = unidadService;
        this.bonusService = bonusService;
        this.calificacionService = calificacionService;
        this.resultadoService = resultadoService;
    }

    // ==========================================
    // GENERACIÓN DE REPORTES
    // ==========================================

    /**
     * Genera el desglose completo de calificaciones de un grupo.
     * @param grupoId ID del grupo a consultar.
     * @return Lista de CalificacionFinal con el detalle por alumno y unidad.
     */
    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId) throws Exception {
        try {
            List<CalificacionFinal> reporteGrupo = new ArrayList<>();
            
            // 1. Obtener la estructura general del grupo
            List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoId);
            List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

            // 2. Procesar el reporte individual de cada alumno
            for (Inscripcion inscripcion : inscripciones) {
                CalificacionFinal calificacionAlumno = procesarCalificacionAlumno(inscripcion, unidades);
                reporteGrupo.add(calificacionAlumno);
            }

            return reporteGrupo;

        } catch (Exception e) {
            throw new Exception("Error al generar el reporte del grupo: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS AUXILIARES (Orquestación Interna)
    // ==========================================

    /**
     * Orquesta el cálculo de todas las unidades y bonus para un solo alumno.
     */
    private CalificacionFinal procesarCalificacionAlumno(Inscripcion inscripcion, List<Unidad> unidades) throws Exception {
        Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
        List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

        // Procesar cada unidad académica delegando al método auxiliar
        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad));
        }

        // Consultar si el alumno tiene puntos extra globales en la materia
        BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Delegar el cálculo matemático final
        return calificacionService.calcularCalificacionFinal(
                inscripcion.getId(), 
                alumno, 
                resultadosUnidades, 
                extraMateria, 
                inscripcion.getCalificacionFinalOverride(), 
                inscripcion.getOverrideJustificacion()
        );
    }

    /**
     * Orquesta la búsqueda de calificaciones y bonus para una sola unidad académica.
     */
    private ResultadoUnidad procesarResultadoUnidad(int inscripcionId, Unidad unidad) throws Exception {
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidad.getId());
        
        BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcionId, unidad.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        return calificacionService.calcularResultadoUnidad(inscripcionId, unidad, resultados, puntosExtra);
    }
}