package com.academico.service;

import com.academico.model.Alumno;
import com.academico.model.Materia;
import com.academico.service.individuals.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaDatosServiceTest {

    @Mock private AlumnoService alumnoService;
    @Mock private MateriaService materiaService;
    @Mock private MaestroService maestroService;
    @Mock private GrupoService grupoService;
    @Mock private InscripcionService inscripcionService;

    @InjectMocks
    private CargaDatosService cargaDatosService;

    private InputStream crearCsvStream(String contenido) {
        return new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Debe delegar el guardado de alumnos al AlumnoService")
    void testImportarAlumnosCsv_Exito() throws Exception {
        String csvFalso = "A001, Juan Perez, juan@test.com\nA002, Ana Gomez";
        InputStream is = crearCsvStream(csvFalso);

        cargaDatosService.importarAlumnosCsv(is);

        verify(alumnoService, times(2)).guardar(any(Alumno.class), eq(false));
    }

    @Test
    void testImportarMateriasCsv_ManejoErrores() throws Exception {
        doThrow(new Exception("Error de prueba"))
            .when(materiaService).guardar(any(Materia.class), eq(false));

        String csvData = "CLAVE,NOMBRE,CREDITOS\nMAT01,Matematicas,8";
        InputStream is = new java.io.ByteArrayInputStream(csvData.getBytes());

        List<String> errores = cargaDatosService.importarMateriasCsv(is);

        assertFalse(errores.isEmpty());
        assertTrue(errores.get(0).contains("Error de prueba"));
    }
}