package com.academico.calificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.academico.inscripciones.Alumno;

class CalificacionServiceTest {

    private CalificacionService service;

    @BeforeEach
    void setUp() {
        service = new CalificacionService();
    }

    // ── Ponderaciones ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Ponderaciones que suman 100 son válidas")
    void ponderacionesValidas_sumaExacta() {
        List<ActividadGrupo> actividades = List.of(
                actividad(50), actividad(30), actividad(20)
        );
        assertTrue(service.ponderacionesValidas(actividades));
    }

    @Test
    @DisplayName("Ponderaciones que no suman 100 son inválidas")
    void ponderacionesValidas_sumaIncompleta() {
        List<ActividadGrupo> actividades = List.of(
                actividad(50), actividad(30)  // suma 80
        );
        assertFalse(service.ponderacionesValidas(actividades));
    }

    @Test
    @DisplayName("Ponderación faltante calcula correctamente")
    void ponderacionFaltante_calculaRestante() {
        List<ActividadGrupo> actividades = List.of(
                actividad(50), actividad(30)  // suma 80, faltan 20
        );
        assertEquals(
            new BigDecimal("20.00"),
            service.ponderacionFaltante(actividades)
        );
    }

    // ── Resultado por unidad ─────────────────────────────────────────────────

    @Test
    @DisplayName("Promedio ponderado se calcula correctamente")
    void calcularResultadoBase_casoNormal() {
        // Examen 85 × 50% + Tarea 90 × 30% + Práctica 70 × 20%
        // = 42.50 + 27.00 + 14.00 = 83.50
        List<Resultado> resultados = List.of(
                resultado(85, 50),
                resultado(90, 30),
                resultado(70, 20)
        );
        assertEquals(
            new BigDecimal("83.50"),
            service.calcularResultadoBase(resultados)
        );
    }

    @Test
    @DisplayName("Actividad no presentada aporta cero al promedio")
    void calcularResultadoBase_conNulo() {
        // Examen 85 × 50% + No presentó × 50%
        // = 42.50 + 0 = 42.50
        List<Resultado> resultados = List.of(
                resultado(85, 50),
                resultadoNulo(50)
        );
        assertEquals(
            new BigDecimal("42.50"),
            service.calcularResultadoBase(resultados)
        );
    }

    @Test
    @DisplayName("Sin calificaciones devuelve null")
    void calcularResultadoBase_todoNulo() {
        List<Resultado> resultados = List.of(
                resultadoNulo(50),
                resultadoNulo(50)
        );
        assertNull(service.calcularResultadoBase(resultados));
    }

    @Test
    @DisplayName("Bonus de unidad se suma al resultado base")
    void aplicarBonusUnidad_sumaCorrectamente() {
        BigDecimal base  = new BigDecimal("83.50");
        BigDecimal bonus = new BigDecimal("2.00");
        assertEquals(
            new BigDecimal("85.50"),
            service.aplicarBonusUnidad(base, bonus)
        );
    }

    @Test
    @DisplayName("Sin bonus devuelve el resultado base sin modificar")
    void aplicarBonusUnidad_sinBonus() {
        BigDecimal base = new BigDecimal("83.50");
        assertEquals(base, service.aplicarBonusUnidad(base, null));
    }

    // ── Calificación final ───────────────────────────────────────────────────

    @Test
    @DisplayName("Promedio de unidades se calcula correctamente")
    void calcularPromedioUnidades_casoNormal() {
        // (85.50 + 78.00 + 91.00) / 3 = 84.83
        List<ResultadoUnidad> unidades = List.of(
                unidadConFinal("85.50"),
                unidadConFinal("78.00"),
                unidadConFinal("91.00")
        );
        assertEquals(
            new BigDecimal("84.83"),
            service.calcularPromedioUnidades(unidades)
        );
    }

    @Test
    @DisplayName("Unidad pendiente se excluye del promedio")
    void calcularPromedioUnidades_conPendiente() {
        // Solo se promedian las unidades con calificación
        // (85.50 + 91.00) / 2 = 88.25
        List<ResultadoUnidad> unidades = List.of(
                unidadConFinal("85.50"),
                unidadPendiente(),
                unidadConFinal("91.00")
        );
        assertEquals(
            new BigDecimal("88.25"),
            service.calcularPromedioUnidades(unidades)
        );
    }

    @Test
    @DisplayName("Override del docente pisa el cálculo automático")
    void calcularCalificacionFinal_override() {
        Alumno alumno = new Alumno(1, null, "A001", "Juan Pérez", "juan@test.com");
        List<ResultadoUnidad> unidades = List.of(unidadConFinal("65.00"));

        CalificacionFinal cf = service.calcularCalificacionFinal(
                1, alumno, unidades,
                null,                       // sin bonus materia
                new BigDecimal("75.00"),    // override
                "Participación extra"
        );

        assertEquals(new BigDecimal("75.00"), cf.getCalificacionFinal());
        assertTrue(cf.isEsOverride());
        assertEquals("Participación extra", cf.getOverrideJustificacion());
    }

    @Test
    @DisplayName("Sin override usa el cálculo automático")
    void calcularCalificacionFinal_sinOverride() {
        Alumno alumno = new Alumno(1, null, "A001", "Ana López", "ana@test.com");
        List<ResultadoUnidad> unidades = List.of(
                unidadConFinal("80.00"),
                unidadConFinal("90.00")
        );

        CalificacionFinal cf = service.calcularCalificacionFinal(
                1, alumno, unidades, null, null, null
        );

        assertEquals(new BigDecimal("85.00"), cf.getCalificacionFinal());
        assertFalse(cf.isEsOverride());
    }

    // ── Estado ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Calificación >= 70 es APROBADO")
    void determinarEstado_aprobado() {
        assertEquals("APROBADO",
            service.determinarEstado(
                new BigDecimal("70.00"),
                new BigDecimal("70")));
    }

    @Test
    @DisplayName("Calificación < 70 es REPROBADO")
    void determinarEstado_reprobado() {
        assertEquals("REPROBADO",
            service.determinarEstado(
                new BigDecimal("69.99"),
                new BigDecimal("70")));
    }

    @Test
    @DisplayName("Calificación null es PENDIENTE")
    void determinarEstado_pendiente() {
        assertEquals("PENDIENTE",
            service.determinarEstado(null, new BigDecimal("70")));
    }

    // ── Helpers de construcción ──────────────────────────────────────────────

    private ActividadGrupo actividad(double ponderacion) {
        ActividadGrupo a = new ActividadGrupo();
        a.setPonderacion(BigDecimal.valueOf(ponderacion));
        return a;
    }

    private Resultado resultado(double calificacion, double ponderacion) {
        Resultado r = new Resultado();
        r.setCalificacion(BigDecimal.valueOf(calificacion));
        r.setPonderacion(BigDecimal.valueOf(ponderacion));
        return r;
    }

    private Resultado resultadoNulo(double ponderacion) {
        Resultado r = new Resultado();
        r.setCalificacion(null);
        r.setPonderacion(BigDecimal.valueOf(ponderacion));
        return r;
    }

    private ResultadoUnidad unidadConFinal(String valor) {
        ResultadoUnidad ru = new ResultadoUnidad();
        ru.setResultadoFinal(new BigDecimal(valor));
        return ru;
    }

    private ResultadoUnidad unidadPendiente() {
        ResultadoUnidad ru = new ResultadoUnidad();
        ru.setResultadoFinal(null);
        return ru;
    }
}