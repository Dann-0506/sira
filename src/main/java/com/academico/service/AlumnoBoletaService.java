package com.academico.service;

import com.academico.model.*;
import com.academico.service.individuals.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AlumnoBoletaService {

    // === SERVICIOS INDIVIDUALES ===
    private final ReporteService reporteService = new ReporteService();
    private final UnidadService unidadService = new UnidadService();
    private final CalificacionService calificacionService = new CalificacionService();
    private final ActividadGrupoService actividadService = new ActividadGrupoService();
    private final ResultadoService resultadoService = new ResultadoService();
    private final BonusService bonusService = new BonusService();

    // === DTOs (Records de Java 21) ===
    public record BoletaDTO(
        int inscripcionId,
        String promedioBase,
        String bonus,
        boolean tieneBonus,
        String calificacionFinal,
        String estadoFinal,
        boolean esOverride,
        String justificacionOverride,
        List<FilaUnidadDTO> filasUnidades
    ) {}

    public record FilaUnidadDTO(int unidadId, String nombreUnidad, String calificacion, String estado) {}

    public record DetalleActividadDTO(String nombre, double ponderacion, String calificacion) {}

    public record DesgloseUnidadDTO(
        List<DetalleActividadDTO> actividades,
        boolean tieneBonus,
        String puntosBonus,
        String justificacionBonus
    ) {}

    // === LÓGICA ORQUESTADA ===

    public BoletaDTO obtenerBoletaGlobal(Grupo curso, Alumno alumno) throws Exception {
        // 1. Obtener Reporte Global y filtrar al alumno
        List<CalificacionFinal> reporte = reporteService.generarReporteFinalGrupo(curso.getId());
        CalificacionFinal misDatos = reporte.stream()
            .filter(cf -> cf.getAlumnoMatricula().equals(alumno.getMatricula()))
            .findFirst()
            .orElseThrow(() -> new Exception("No se encontraron calificaciones para este alumno."));

        // 2. Construir Filas de Unidades
        List<Unidad> unidades = unidadService.listarPorMateria(curso.getMateriaId());
        List<FilaUnidadDTO> filas = new ArrayList<>();
        
        for (Unidad u : unidades) {
            String califStr = "-";
            String estadoStr = "PENDIENTE";
            
            Optional<ResultadoUnidad> resU = misDatos.getUnidades().stream()
                .filter(ru -> ru.getUnidadId() == u.getId())
                .findFirst();
                
            if (resU.isPresent() && resU.get().getResultadoFinal() != null) {
                califStr = resU.get().getResultadoFinal().toString();
                estadoStr = calificacionService.determinarEstado(resU.get().getResultadoFinal());
            }
            
            String tituloCompleto = "U" + u.getNumero() + " - " + u.getNombre();
            filas.add(new FilaUnidadDTO(u.getId(), tituloCompleto, califStr, estadoStr));
        }

        // 3. Resumen Final
        String promBase = misDatos.getCalificacionCalculada() != null ? misDatos.getCalificacionCalculada().toString() : "-";
        
        boolean tieneBonus = misDatos.getBonusMateria() != null && misDatos.getBonusMateria().compareTo(BigDecimal.ZERO) > 0;
        String bonusStr = tieneBonus ? "+" + misDatos.getBonusMateria().toString() + " pts" : "0.00 pts";
        
        String califFinal = misDatos.getCalificacionFinal() != null ? misDatos.getCalificacionFinal().toString() : "-";
        
        // Determinar Estado (Calculado en tiempo real por el momento para evitar errores de compilación)
        String estadoFinal = "PENDIENTE";
        if (misDatos.getCalificacionFinal() != null) {
            estadoFinal = calificacionService.determinarEstado(misDatos.getCalificacionFinal());
        }

        return new BoletaDTO(
            misDatos.getInscripcionId(),
            promBase,
            bonusStr,
            tieneBonus,
            califFinal,
            estadoFinal,
            misDatos.isEsOverride(),
            misDatos.getOverrideJustificacion(),
            filas
        );
    }

    public DesgloseUnidadDTO obtenerDesgloseUnidad(int inscripcionId, int grupoId, int unidadId) throws Exception {
        // 1. Cruzar Actividades y Resultados
        List<ActividadGrupo> actividades = actividadService.buscarPorGrupoYUnidad(grupoId, unidadId);
        List<Resultado> resultados = resultadoService.buscarPorInscripcionYUnidad(inscripcionId, unidadId);
        
        List<DetalleActividadDTO> detallesAct = new ArrayList<>();
        for (ActividadGrupo ag : actividades) {
            String notaStr = "Sin calificar";
            Optional<Resultado> resOpt = resultados.stream()
                .filter(r -> r.getActividadGrupoId() == ag.getId())
                .findFirst();
                
            if (resOpt.isPresent() && resOpt.get().getCalificacion() != null) {
                notaStr = resOpt.get().getCalificacion().toString();
            }
            detallesAct.add(new DetalleActividadDTO(ag.getNombre(), ag.getPonderacion().doubleValue(), notaStr));
        }

        // 2. Obtener Bonus (CORREGIDO: Usando el método nativo del BonusService)
        Optional<Bonus> bonusOpt = bonusService.obtenerBonusUnidad(inscripcionId, unidadId);
        
        boolean tieneB = false;
        String pB = "";
        String jB = "";
        
        if (bonusOpt.isPresent()) {
            tieneB = true;
            pB = "+" + bonusOpt.get().getPuntos().toString() + " pts";
            jB = bonusOpt.get().getJustificacion();
        }
        
        return new DesgloseUnidadDTO(detallesAct, tieneB, pB, jB);
    }
}