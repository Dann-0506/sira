package com.academico.inscripciones;

import com.academico.academia.GrupoDAO;
import com.academico.academia.Materia;
import com.academico.academia.MateriaDAO;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CargaDatosServiceTest {

    @Mock private AlumnoDAO alumnoDAO;
    @Mock private MateriaDAO materiaDAO;
    @Mock private GrupoDAO grupoDAO;
    @Mock private InscripcionDAO inscripcionDAO;

    @InjectMocks
    private CargaDatosService cargaDatosService;

    @Captor
    private ArgumentCaptor<List<Alumno>> alumnoCaptor;

    @Captor
    private ArgumentCaptor<List<Materia>> materiaCaptor;

    private InputStream crearCsvStream(String contenido) {
        return new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Debe leer el CSV de alumnos y enviarlo al DAO correctamente")
    void testImportarAlumnosCsv_Exito() throws IOException, CsvValidationException, SQLException {
        // Simular el contenido de un archivo CSV
        String csvFalso = """
                A001, Juan Perez, juan@test.com
                A002, Ana Gomez,  
                A003
                """;
        InputStream is = crearCsvStream(csvFalso);

        when(alumnoDAO.insertarLote(anyList())).thenReturn(Collections.emptyList());

        cargaDatosService.importarAlumnosCsv(is);

        verify(alumnoDAO, times(1)).insertarLote(alumnoCaptor.capture());
        List<Alumno> alumnosEnviados = alumnoCaptor.getValue();

        assertEquals(2, alumnosEnviados.size(), "Debería ignorar la tercera fila por estar incompleta");
        
        assertEquals("A001", alumnosEnviados.get(0).getMatricula());
        assertEquals("Juan Perez", alumnosEnviados.get(0).getNombre());
        assertEquals("juan@test.com", alumnosEnviados.get(0).getEmail());
        
        assertEquals("A002", alumnosEnviados.get(1).getMatricula());
        assertNull(alumnosEnviados.get(1).getEmail(), "Si el email viene vacío, no debe guardarse");
    }

    @Test
    @DisplayName("Debe leer el CSV de materias e ignorar filas con errores numéricos")
    void testImportarMateriasCsv_ManejoDeErrores() throws IOException, CsvValidationException, SQLException {
        // Simular CSV: La segunda materia tiene "letras" en vez de un número de unidades
        String csvFalso = """
                MAT1, Matematicas, 5
                MAT2, Fisica, letras
                MAT3, Quimica, 4
                """;
        InputStream is = crearCsvStream(csvFalso);

        when(materiaDAO.insertarLote(anyList())).thenReturn(Collections.emptyList());

        cargaDatosService.importarMateriasCsv(is);

        verify(materiaDAO, times(1)).insertarLote(materiaCaptor.capture());
        List<Materia> materiasEnviadas = materiaCaptor.getValue();

        assertEquals(2, materiasEnviadas.size(), "Debería ignorar la materia 'Fisica' por error numérico");
        
        assertEquals("MAT1", materiasEnviadas.get(0).getClave());
        assertEquals(5, materiasEnviadas.get(0).getTotalUnidades());
        
        assertEquals("MAT3", materiasEnviadas.get(1).getClave());
        assertEquals(4, materiasEnviadas.get(1).getTotalUnidades());
    }
}