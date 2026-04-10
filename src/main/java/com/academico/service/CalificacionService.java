package com.academico.service;

import com.academico.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de Lógica de Negocio (Pura).
 * Responsabilidad: Realizar cálculos matemáticos de ponderaciones, promedios,
 * aplicación de puntos extra y determinación de estados académicos.
 */
public class CalificacionService {

    // === CONSTANTES MATEMÁTICAS ===
    private static final int ESCALA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;
    private static final BigDecimal MAX_CALIFICACION = new BigDecimal("100.00");
    private static final BigDecimal PONDERACION_TOTAL = new BigDecimal("100.00");

    // ==========================================
    // VALIDACIÓN DE PONDERACIONES (Estructura)
    // ==========================================

    public BigDecimal sumarPonderaciones(List<ActividadGrupo> actividades) {
        if (actividades == null || actividades.isEmpty()) return BigDecimal.ZERO;
        
        return actividades.stream()
                .map(ActividadGrupo::getPonderacion)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }

    public boolean ponderacionesValidas(List<ActividadGrupo> actividades) {
        return sumarPonderaciones(actividades).compareTo(PONDERACION_TOTAL) == 0;
    }

    public BigDecimal ponderacionFaltante(List<ActividadGrupo> actividades) {
        return PONDERACION_TOTAL
                .subtract(sumarPonderaciones(actividades))
                .setScale(ESCALA, REDONDEO);
    }

    // ==========================================
    // CÁLCULO DE RESULTADOS (Por Unidad)
    // ==========================================

    public BigDecimal calcularResultadoBase(List<Resultado> resultados) {   
        if (resultados == null || resultados.isEmpty()) return null;

        // Si no hay ninguna calificación registrada aún, devolvemos null (Pendiente)
        boolean hayAlgunaCalificacion = resultados.stream()
                .anyMatch(r -> r.getCalificacion() != null);
        if (!hayAlgunaCalificacion) return null;

        return resultados.stream()
                .map(Resultado::getAportacion)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusUnidad(BigDecimal resultadoBase, BigDecimal bonusPuntos) {
        if (resultadoBase == null) return null;
        if (bonusPuntos == null || bonusPuntos.compareTo(BigDecimal.ZERO) <= 0) return resultadoBase;

        // Límite estricto: La calificación con bonus no puede exceder 100.00
        BigDecimal resultadoConBonus = resultadoBase.add(bonusPuntos);
        return resultadoConBonus.min(MAX_CALIFICACION).setScale(ESCALA, REDONDEO);
    }

    public ResultadoUnidad calcularResultadoUnidad(
        int inscripcionId,
        Unidad unidad,
        List<Resultado> resultados,
        BigDecimal bonusPuntos
    ) {
        BigDecimal base = calcularResultadoBase(resultados);
        BigDecimal bonus = bonusPuntos != null ? bonusPuntos : BigDecimal.ZERO;
        BigDecimal final_ = aplicarBonusUnidad(base, bonusPuntos);
        
        long calificadas = resultados == null ? 0 : resultados.stream()
                .filter(r -> r.getCalificacion() != null)
                .count();
        
        ResultadoUnidad ru = new ResultadoUnidad();
        ru.setInscripcionId(inscripcionId);
        ru.setUnidadId(unidad.getId());
        ru.setUnidadNumero(unidad.getNumero());
        ru.setUnidadNombre(unidad.getNombre());
        ru.setDesglose(resultados);
        ru.setResultadoBase(base);
        ru.setBonusPuntos(bonus);
        ru.setResultadoFinal(final_);
        ru.setActividadesCalificadas((int) calificadas);
        ru.setActividadesTotales(resultados == null ? 0 : resultados.size());
        
        return ru;
    }

    // ==========================================
    // CÁLCULO DE CALIFICACIÓN FINAL (Materia Completa)
    // ==========================================

    public BigDecimal calcularPromedioUnidades(List<ResultadoUnidad> unidades) {
        if (unidades == null || unidades.isEmpty()) return null;

        List<BigDecimal> finales = unidades.stream()
                .map(ResultadoUnidad::getResultadoFinal)
                .filter(Objects::nonNull) // Null-safety
                .toList();
        
        if (finales.isEmpty()) return null;

        BigDecimal suma = finales.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(finales.size()), ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusMateria(BigDecimal promedio, BigDecimal bonusPuntos) {
        if (promedio == null) return null;
        if (bonusPuntos == null || bonusPuntos.compareTo(BigDecimal.ZERO) <= 0) return promedio;

        // Límite estricto: La calificación final con bonus no puede exceder 100.00
        BigDecimal resultadoConBonus = promedio.add(bonusPuntos);
        return resultadoConBonus.min(MAX_CALIFICACION).setScale(ESCALA, REDONDEO);
    }

    public CalificacionFinal calcularCalificacionFinal(
        int inscripcionId,
        Alumno alumno,
        List<ResultadoUnidad> unidades,
        BigDecimal bonusMateria,
        BigDecimal override,
        String overrideJustificacion
    ) {
        BigDecimal calculada = calcularPromedioUnidades(unidades);
        BigDecimal bonus = bonusMateria != null ? bonusMateria : BigDecimal.ZERO;
        BigDecimal conBonus = aplicarBonusMateria(calculada, bonus);
        
        // Si el maestro hizo un "Override" (Calificación manual), esa tiene prioridad absoluta
        BigDecimal definitiva = override != null ? override : conBonus;

        CalificacionFinal cf = new CalificacionFinal();
        cf.setInscripcionId(inscripcionId);
        cf.setAlumnoId(alumno.getId());
        cf.setAlumnoNombre(alumno.getNombre());
        cf.setAlumnoMatricula(alumno.getMatricula());
        cf.setUnidades(unidades);
        cf.setCalificacionCalculada(calculada);
        cf.setBonusMateria(bonus);
        cf.setCalificacionConBonus(conBonus);
        cf.setCalificacionFinal(definitiva);
        cf.setEsOverride(override != null);
        cf.setOverrideJustificacion(overrideJustificacion);
        
        return cf;
    }

    // ==========================================
    // ESTADO ACADÉMICO
    // ==========================================

    public String determinarEstado(BigDecimal calificacion, BigDecimal minimoAprobatorio) {
        if (calificacion == null) return "PENDIENTE";
        return calificacion.compareTo(minimoAprobatorio) >= 0 ? "APROBADO" : "REPROBADO";
    }
}