package com.academico.calificaciones;

import com.academico.academia.Unidad;
import com.academico.inscripciones.Alumno;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalificacionService {

    private static final int ESCALA = 2;
    private static final RoundingMode REDONDEO = RoundingMode.HALF_UP;

    // === Validación de ponderaciones ===

    public BigDecimal sumarPonderaciones(List<ActividadGrupo> actividades) {
        return actividades.stream()
                .map(ActividadGrupo::getPonderacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }


    public boolean ponderacionesValidas(List<ActividadGrupo> actividades) {
        return sumarPonderaciones(actividades)
                .compareTo(BigDecimal.valueOf(100)) == 0;
    }


    public BigDecimal ponderacionFaltante(List<ActividadGrupo> actividades) {
        return BigDecimal.valueOf(100)
                .subtract(sumarPonderaciones(actividades))
                .setScale(ESCALA, REDONDEO);
    }


    // === Cálculo de resultados por unidad ===

    public BigDecimal calcularResultadoBase(List<Resultado> resultados) {   
        if (resultados == null || resultados.isEmpty()) return null;

        boolean hayAlgunaCalificacion = resultados.stream()
                .anyMatch(r -> r.getCalificacion() != null);

        if (!hayAlgunaCalificacion) return null;

        return resultados.stream()
                .map(Resultado::getAportacion)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusUnidad(BigDecimal resultadoBase, BigDecimal bonusPuntos) {
        if (resultadoBase == null) return null;
        if (bonusPuntos == null) return resultadoBase;

        return resultadoBase.add(bonusPuntos).setScale(ESCALA, REDONDEO);
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
        
        long calificadas = resultados.stream()
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
        ru.setActividadesTotales(resultados.size());
        return ru;
    }


    // === Cálculo de calificación final ===

    public BigDecimal calcularPromedioUnidades(List<ResultadoUnidad> unidades) {
        if (unidades == null || unidades.isEmpty()) return null;

        List<BigDecimal> finales = unidades.stream()
                .map(ResultadoUnidad::getResultadoFinal)
                .filter(r -> r != null)
                .toList();
        
        if (finales.isEmpty()) return null;

        BigDecimal suma = finales.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return suma.divide(BigDecimal.valueOf(finales.size()), ESCALA, REDONDEO);
    }

    public BigDecimal aplicarBonusMateria(BigDecimal promedio, BigDecimal bonusPuntos) {
        if (promedio == null) return null;
        if (bonusPuntos == null) return promedio;

        return promedio.add(bonusPuntos).setScale(ESCALA, REDONDEO);
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


    // === Determinación de estado ===

    public String determinarEstado(BigDecimal calificacion, BigDecimal minimoAprobatorio) {
        if (calificacion == null) return "PENDIENTE";
        return calificacion.compareTo(minimoAprobatorio) >= 0 ? "APROBADO" : "REPROBADO";
    }
}
