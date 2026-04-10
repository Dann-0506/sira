package com.academico.service;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;
import com.academico.service.individuals.EstadoUnidadService;

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
    @DisplayName("Debe guardar la actividad si la suma de ponderaciones es <= 100")
    void testGuardarActividad_Exito() throws Exception {
        // Preparar: Simular que actualmente hay 50% registrado en la base de datos
        when(actividadDAO.sumaPonderaciones(1, 1)).thenReturn(new BigDecimal("50.00"));
        // Simular que el DAO devuelve la actividad guardada con un ID asignado
        when(actividadDAO.insertar(any())).thenReturn(actividadPrueba);

        // Ejecutar
        ActividadGrupo resultado = estructuraService.guardarActividad(actividadPrueba);

        // Verificar: Asegurar que el DAO sí fue llamado y no se lanzó ninguna excepción
        assertNotNull(resultado, "Debería devolver la actividad guardada");
        verify(actividadDAO, times(1)).insertar(actividadPrueba);
    }

    @Test
    @DisplayName("Debe rechazar el guardado si la suma excede 100")
    void testGuardarActividad_FallaExcede100() throws SQLException {
        // Preparar: Simular que actualmente hay 80% registrado (80 + 30 = 110%)
        when(actividadDAO.sumaPonderaciones(1, 1)).thenReturn(new BigDecimal("80.00"));

        // Ejecutar y Verificar: Comprobar que se lance la excepción genérica con nuestro mensaje
        Exception exception = assertThrows(Exception.class, () -> {
            estructuraService.guardarActividad(actividadPrueba);
        });

        assertEquals("Error: La suma de las ponderaciones de esta unidad excedería el 100%.", exception.getMessage());
        
        // Confirmar que el intento de insertar nunca llegó a la base de datos
        verify(actividadDAO, never()).insertar(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si se intenta guardar en una unidad cerrada")
    void testGuardarActividad_FallaUnidadCerrada() throws SQLException {
        // Preparar: Simular que el servicio de estado bloquea la operación
        doThrow(new IllegalStateException("Acción no permitida: Unidad cerrada."))
                .when(estadoUnidadService).validarUnidadAbierta(1, 1);

        // Ejecutar y Verificar: Comprobar que detenga la ejecución inmediatamente
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            estructuraService.guardarActividad(actividadPrueba);
        });

        assertEquals("Acción no permitida: Unidad cerrada.", exception.getMessage());
        
        // Confirmar que no llegó ni a validar matemáticas ni a insertar en la BD
        verify(actividadDAO, never()).sumaPonderaciones(anyInt(), anyInt());
        verify(actividadDAO, never()).insertar(any());
    }
}