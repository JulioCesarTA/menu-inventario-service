package com.uagrm.comedor.menu.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PorcionTest {

    @Test
    void cocinar_lo_apartado_mas_diez_por_ciento_redondeado_hacia_arriba() {
        assertEquals(13, new Porcion("p", 100, 11).racionesACocinar());   // 11 + ceil(1.1)
        assertEquals(11, new Porcion("p", 100, 10).racionesACocinar());   // 10 + 1
        assertEquals(2, new Porcion("p", 100, 1).racionesACocinar());     // 1 + ceil(0.1)
    }

    @Test
    void sin_reservas_no_se_cocina_nada() {
        assertEquals(0, new Porcion("p", 100, 0).racionesACocinar());
    }

    @Test
    void nunca_se_cocina_mas_que_el_cupo() {
        assertEquals(20, new Porcion("p", 20, 19).racionesACocinar());
        assertEquals(20, new Porcion("p", 20, 20).racionesACocinar());
    }
}
