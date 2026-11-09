package com.uagrm.comedor.menu.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FechaVencimientoTest {

    private final LocalDate hoy = LocalDate.of(2026, 11, 9);

    @Test
    void vence_hasta_incluye_el_dia_limite() {
        FechaVencimiento f = new FechaVencimiento(hoy.plusDays(3));
        assertTrue(f.venceHasta(hoy.plusDays(3)));
        assertTrue(f.venceHasta(hoy.plusDays(4)));
        assertFalse(f.venceHasta(hoy.plusDays(2)));
    }

    @Test
    void dias_restantes_negativos_si_ya_vencio() {
        assertEquals(3, new FechaVencimiento(hoy.plusDays(3)).diasRestantes(hoy));
        assertEquals(0, new FechaVencimiento(hoy).diasRestantes(hoy));
        assertEquals(-2, new FechaVencimiento(hoy.minusDays(2)).diasRestantes(hoy));
    }
}
