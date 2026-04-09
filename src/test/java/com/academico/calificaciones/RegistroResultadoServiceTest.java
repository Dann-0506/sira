package com.academico.calificaciones;

import com.academico.dao.InscripcionDAO;
import com.academico.dao.ResultadoDAO;
import com.academico.model.Inscripcion;
import com.academico.service.EstadoUnidadService;
import com.academico.service.RegistroResultadoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistroResultadoServiceTest {

    @Mock
    private ResultadoDAO resultadoDAO;

    @Mock
    private InscripcionDAO inscripcionDAO;

    @Mock
    private EstadoUnidadService estadoUnidadService;

    @InjectMocks
    private RegistroResultadoService registroService;

    @Test
    @DisplayName("Debe permitir guardar calificación si la unidad está ABIERTA")
    void testGuardarCalificacion_Exito() throws SQLException {
        // Ejecutar el guardado.
        registroService.guardarCalificacion(1, 1, 1, 1, new BigDecimal("85.00"));

        verify(estadoUnidadService, times(1)).validarUnidadAbierta(1, 1);
        
        verify(resultadoDAO, times(1)).guardar(1, 1, new BigDecimal("85.00"));
    }

    @Test
    @DisplayName("Debe lanzar excepción y bloquear el guardado si la unidad está CERRADA")
    void testGuardarCalificacion_FallaUnidadCerrada() throws SQLException {
        // Simular que la unidad está cerrada
        doThrow(new IllegalStateException("Unidad cerrada."))
                .when(estadoUnidadService).validarUnidadAbierta(1, 1);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            registroService.guardarCalificacion(1, 1, 1, 1, new BigDecimal("85.00"));
        });

        assertEquals("Unidad cerrada.", exception.getMessage());
        
        verify(resultadoDAO, never()).guardar(anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Debe permitir aplicar Override si la inscripción existe")
    void testAplicarOverride_Exito() throws SQLException {
        // Simular que la base de datos sí encontró la inscripción
        Inscripcion inscripcionMock = new Inscripcion();
        when(inscripcionDAO.findById(10)).thenReturn(Optional.of(inscripcionMock));

        registroService.aplicarOverrideMateria(10, new BigDecimal("90.00"), "Excelente proyecto");

        // Verificar que se llamó a la actualización en la BD
        verify(inscripcionDAO, times(1)).actualizarOverride(10, new BigDecimal("90.00"), "Excelente proyecto");
    }

    @Test
    @DisplayName("Debe bloquear el Override si la inscripción NO existe")
    void testAplicarOverride_FallaInscripcionNoExiste() throws SQLException {
        // Simular que la base de datos NO encontró al alumno
        when(inscripcionDAO.findById(99)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            registroService.aplicarOverrideMateria(99, new BigDecimal("90.00"), "Fake");
        });

        assertEquals("La inscripción no existe.", exception.getMessage());
        
        verify(inscripcionDAO, never()).actualizarOverride(anyInt(), any(), anyString());
    }
}
