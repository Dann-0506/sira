package com.academico.service;

import com.academico.dao.*;
import com.academico.model.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReporteService {

    private final InscripcionDAO inscripcionDAO;
    private final ResultadoDAO resultadoDAO;
    private final UnidadDAO unidadDAO;
    private final BonusDAO bonusDAO;
    private final CalificacionService calificacionService;
    private final AlumnoDAO alumnoDAO;

    public ReporteService() {
        this.inscripcionDAO = new InscripcionDAO();
        this.resultadoDAO = new ResultadoDAO();
        this.unidadDAO = new UnidadDAO();
        this.bonusDAO = new BonusDAO();
        this.calificacionService = new CalificacionService();
        this.alumnoDAO = new AlumnoDAO();
    }

    public List<CalificacionFinal> generarReporteFinalGrupo(int grupoId) throws SQLException {
        List<CalificacionFinal> reporteGrupo = new ArrayList<>();
        
        List<Inscripcion> inscripciones = inscripcionDAO.findByGrupo(grupoId);
        List<Unidad> unidades = unidadDAO.findByGrupo(grupoId);

        for (Inscripcion inscripcion : inscripciones) {
            
            Alumno alumno = alumnoDAO.findById(inscripcion.getAlumnoId()).orElse(new Alumno());
            
            List<ResultadoUnidad> resultadosUnidades = new ArrayList<>();

            for (Unidad unidad : unidades) {
                List<Resultado> resultados = resultadoDAO.findByInscripcionYUnidad(inscripcion.getId(), unidad.getId());
                Optional<Bonus> bonusUnidad = bonusDAO.findByInscripcionYUnidad(inscripcion.getId(), unidad.getId());
                BigDecimal puntosExtra = bonusUnidad.map(Bonus::getPuntos).orElse(null);

                ResultadoUnidad ru = calificacionService.calcularResultadoUnidad(
                        inscripcion.getId(), unidad, resultados, puntosExtra);
                
                resultadosUnidades.add(ru);
            }

            Optional<Bonus> bonusMateria = bonusDAO.findBonusMateria(inscripcion.getId());
            BigDecimal extraMateria = bonusMateria.map(Bonus::getPuntos).orElse(null);

            CalificacionFinal calificacionFinal = calificacionService.calcularCalificacionFinal(
                    inscripcion.getId(), 
                    alumno, 
                    resultadosUnidades, 
                    extraMateria, 
                    inscripcion.getCalificacionFinalOverride(), 
                    inscripcion.getOverrideJustificacion()
            );

            reporteGrupo.add(calificacionFinal);
        }

        return reporteGrupo;
    }
}