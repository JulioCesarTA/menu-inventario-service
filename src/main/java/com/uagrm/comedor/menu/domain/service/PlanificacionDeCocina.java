package com.uagrm.comedor.menu.domain.service;

import com.uagrm.comedor.menu.domain.model.Menu;
import com.uagrm.comedor.menu.domain.model.Porcion;
import com.uagrm.comedor.menu.domain.model.Turno;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Servicio de dominio: junta las porciones de un plato repartidas en varios menús (agregados distintos)
 * de un mismo turno, para saber cuánto cocinar. La regla del margen vive en {@link Porcion#racionesACocinar()}.
 */
public class PlanificacionDeCocina {

    /** platoId -> porción combinada (cupo y apartadas sumados) de los menús de ese turno, ordenado por platoId. */
    public Map<String, Porcion> porcionesDelTurno(Collection<Menu> menus, Turno turno) {
        Map<String, Porcion> platos = new TreeMap<>();
        menus.stream().filter(m -> m.getTurno() == turno).forEach(m ->
            m.getPorciones().forEach((platoId, p) -> platos.merge(platoId,
                new Porcion(platoId, p.cupoTotal(), p.apartadas()),
                (a, b) -> new Porcion(platoId, a.cupoTotal() + b.cupoTotal(), a.apartadas() + b.apartadas()))));
        return platos;
    }
}
