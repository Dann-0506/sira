package com.academico.service.individuals;

import com.academico.dao.ActividadGrupoDAO;
import com.academico.model.ActividadGrupo;
import java.sql.SQLException;
import java.util.List;

public class ActividadGrupoService {
    
    private final ActividadGrupoDAO actividadDAO = new ActividadGrupoDAO();

    public List<ActividadGrupo> buscarPorGrupoYUnidad(int grupoId, int unidadId) throws Exception {
        try {
            return actividadDAO.findByGrupoYUnidad(grupoId, unidadId);
        } catch (SQLException e) {
            throw new Exception("Error al cargar las actividades.");
        }
    }

    public void guardar(ActividadGrupo actividad) throws Exception {
        try {
            if (actividad.getId() == 0) {
                actividadDAO.insertar(actividad);
            } else {
                actividadDAO.actualizar(actividad);
            }
        } catch (SQLException e) {
            throw new Exception("Error al guardar la actividad.");
        }
    }

    public void eliminar(int id) throws Exception {
        try {
            actividadDAO.eliminar(id);
        } catch (SQLException e) {
            throw new Exception("Error al eliminar la actividad.");
        }
    }
}