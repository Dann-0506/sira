package com.academico.service.individuals;

import com.academico.dao.InscripcionDAO;
import com.academico.model.Inscripcion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock private InscripcionDAO inscripcionDAO;

    @InjectMocks
    private InscripcionService inscripcionService;

    @Test
    @DisplayName("Debe aplicar override cuando la inscripción existe")
    void testAplicarOverride_Exito() throws Exception {
        when(inscripcionDAO.findById(1)).thenReturn(Optional.of(new Inscripcion()));
        
        inscripcionService.aplicarOverrideMateria(1, new BigDecimal("9.0"), "Mérito");

        verify(inscripcionDAO).actualizarOverride(1, new BigDecimal("9.0"), "Mérito");
    }

    @Test
    void testAplicarOverride_NoExiste() {
        Exception exception = assertThrows(Exception.class, () -> {
            inscripcionService.aplicarOverrideMateria(999, new BigDecimal("80.00"), "Justificación válida");
        });

        assertTrue(exception.getMessage().contains("ya no existe"));
    }
}