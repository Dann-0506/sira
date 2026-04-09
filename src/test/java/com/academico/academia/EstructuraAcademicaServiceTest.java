package com.academico.academia;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;
import com.academico.service.EstadoUnidadService;
import com.academico.service.EstructuraAcademicaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstructuraAcademicaServiceTest {

    @Mock
    private ActividadGrupoDAO actividadDAO;

    @Mock
    private EstadoUnidadService estadoUnidadService;

    @InjectMocks
    private EstructuraAcademicaService estructuraService;

    private ActividadGrupo actividadPrueba;

    @BeforeEach
    void setUp() {
        actividadPrueba = new ActividadGrupo();
        actividadPrueba.setGrupoId(1);
        actividadPrueba.setUnidadId(1);
        actividadPrueba.setPonderacion(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Debe permitir agregar si la suma total es <= 100")
    void testPuedeAgregarActividad_Exito() throws SQLException {
        // Simular que la base de datos dice que actualmente hay 50% registrado
        when(actividadDAO.sumaPonderaciones(1, 1)).thenReturn(new BigDecimal("50.00"));

        // Intentar agregar 30% más (Total 80%)
        boolean resultado = estructuraService.puedeAgregarActividad(1, 1, new BigDecimal("30.00"));

        assertTrue(resultado, "Debería permitir agregar porque 50 + 30 <= 100");
    }

    @Test
    @DisplayName("Debe rechazar si la suma total excede 100")
    void testPuedeAgregarActividad_FallaExcede100() throws SQLException {
        // Simular que la base de datos dice que actualmente hay 80% registrado
        when(actividadDAO.sumaPonderaciones(1, 1)).thenReturn(new BigDecimal("80.00"));

        // Intentar agregar 30% más (Total 110%)
        boolean resultado = estructuraService.puedeAgregarActividad(1, 1, new BigDecimal("30.00"));

        assertFalse(resultado, "Debería rechazar porque 80 + 30 > 100");
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta guardar en una unidad cerrada")
    void testGuardarActividad_FallaUnidadCerrada() throws SQLException {
        // Simular que la unidad está cerrada 
        doThrow(new IllegalStateException("Unidad cerrada."))
                .when(estadoUnidadService).validarUnidadAbierta(1, 1);

        // Verificar que al intentar guardar, la excepción suba y detenga todo
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            estructuraService.guardarActividad(actividadPrueba);
        });

        assertEquals("Unidad cerrada.", exception.getMessage());
        
        verify(actividadDAO, never()).insertar(any());
    }
}