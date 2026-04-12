package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ReporteService {

    private final AlumnoService alumnoService = new AlumnoService();
    private final InscripcionService inscripcionService = new InscripcionService();
    private final UnidadService unidadService = new UnidadService();
    private final BonusService bonusService = new BonusService();
    private final CalificacionService calificacionService = new CalificacionService();
    private final ResultadoService resultadoService = new ResultadoService();

    /**
     * Genera el reporte completo de calificaciones para un grupo.
     * Este método centraliza la lógica que antes repetías en los bucles de los controladores.
     */
    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId, BigDecimal limiteMaximoGrupo) throws Exception {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        // 1. Obtener estructura y alumnos inscritos
        List<Inscripcion> inscripciones = inscripcionService.listarPorGrupo(grupoId);
        List<Unidad> unidades = unidadService.listarPorGrupo(grupoId);

        // 2. Procesar cada alumno pasándole la regla (límite) histórica
        for (Inscripcion inscripcion : inscripciones) {
            reporteGrupo.add(procesarCalificacionAlumno(inscripcion, unidades, limiteMaximoGrupo));
        }

        return reporteGrupo;
    }

    private CalificacionFinal procesarCalificacionAlumno(Inscripcion inscripcion, List<Unidad> unidades, BigDecimal limiteMaximo) throws Exception {
        
        // === NUEVA LÓGICA DE LECTURA HISTÓRICA ===
        // Si el acta ya se cerró en el pasado, devolvemos la "fotografía" de la Base de Datos
        if (inscripcion.getCalificacionFinalCalculada() != null) {
            Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
            CalificacionFinal cfHistorica = new CalificacionFinal();
            
            cfHistorica.setInscripcionId(inscripcion.getId());
            cfHistorica.setAlumnoId(alumno.getId());
            cfHistorica.setAlumnoNombre(alumno.getNombre());
            cfHistorica.setAlumnoMatricula(alumno.getMatricula());
            
            // Cargamos el desglose visual de unidades (para el reporte final del PDF y boletas)
            List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();
            for (Unidad unidad : unidades) {
                resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad, limiteMaximo));
            }
            cfHistorica.setUnidades(resultadosUnidades);

            // Inyectamos el valor histórico (sin hacer ningún cálculo)
            cfHistorica.setCalificacionCalculada(inscripcion.getCalificacionFinalCalculada());
            
            // Si el maestro aplicó un Override manual antes de cerrar el acta, se respeta la decisión
            BigDecimal finalDefinitiva = inscripcion.getCalificacionFinalOverride() != null 
                    ? inscripcion.getCalificacionFinalOverride() 
                    : inscripcion.getCalificacionFinalCalculada();
                    
            cfHistorica.setCalificacionFinal(finalDefinitiva);
            cfHistorica.setEsOverride(inscripcion.getCalificacionFinalOverride() != null);
            cfHistorica.setOverrideJustificacion(inscripcion.getOverrideJustificacion());
            
            return cfHistorica;
        }

        // =========================================
        // SI LLEGA AQUÍ, EL GRUPO ESTÁ ABIERTO:
        // Continúa con la lógica normal de cálculo on-demand
        
        Alumno alumno = alumnoService.buscarPorId(inscripcion.getAlumnoId());
        List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

        for (Unidad unidad : unidades) {
            resultadosUnidades.add(procesarResultadoUnidad(inscripcion.getId(), unidad, limiteMaximo));
        }

        BigDecimal extraMateria = bonusService.obtenerBonusMateria(inscripcion.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        return calificacionService.calcularCalificacionFinal(
                inscripcion.getId(), 
                alumno, 
                resultadosUnidades, 
                extraMateria, 
                inscripcion.getCalificacionFinalOverride(), 
                inscripcion.getOverrideJustificacion(),
                limiteMaximo 
        );
    }

    private ResultadoUnidad procesarResultadoUnidad(int inscripcionId, Unidad unidad, BigDecimal limiteMaximo) throws Exception {
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidad.getId());
        
        BigDecimal puntosExtra = bonusService.obtenerBonusUnidad(inscripcionId, unidad.getId())
                .map(Bonus::getPuntos)
                .orElse(BigDecimal.ZERO);

        // Llama al método pasando el límite en lugar de que el servicio lo busque
        return calificacionService.calcularResultadoUnidad(inscripcionId, unidad, resultados, puntosExtra, limiteMaximo); // <--- NUEVO PARÁMETRO INYECTADO
    }
}