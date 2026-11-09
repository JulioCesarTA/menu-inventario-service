package com.uagrm.comedor.menu.domain.service;

import com.uagrm.comedor.menu.domain.model.Menu;
import com.uagrm.comedor.menu.domain.model.Porcion;
import com.uagrm.comedor.menu.domain.model.Turno;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlanificacionDeCocinaTest {

    private final LocalDate dia = LocalDate.of(2026, 11, 9);

    private Menu menu(String id, Turno turno, String platoId, int cupo, int apartadas) {
        return Menu.reconstituir(id, dia, turno, true, Map.of(platoId, new Porcion(platoId, cupo, apartadas)));
    }

    @Test
    void suma_las_porciones_del_mismo_plato_solo_del_turno_pedido() {
        var r = new PlanificacionDeCocina().porcionesDelTurno(List.of(
            menu("m1", Turno.ALMUERZO, "pollo", 20, 11),
            menu("m2", Turno.ALMUERZO, "pollo", 10, 4),
            menu("m3", Turno.CENA, "pollo", 50, 50),
            menu("m4", Turno.ALMUERZO, "arroz", 5, 1)), Turno.ALMUERZO);
        assertEquals(List.of("arroz", "pollo"), List.copyOf(r.keySet()));   // ordenado por platoId
        assertEquals(new Porcion("pollo", 30, 15), r.get("pollo"));
        assertEquals(17, r.get("pollo").racionesACocinar());               // 15 + ceil(1.5)
    }

    @Test
    void sin_menus_del_turno_no_hay_nada_que_cocinar() {
        assertTrue(new PlanificacionDeCocina().porcionesDelTurno(
            List.of(menu("m1", Turno.CENA, "sopa", 10, 2)), Turno.DESAYUNO).isEmpty());
    }
}
