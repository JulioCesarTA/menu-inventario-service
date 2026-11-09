package com.uagrm.comedor.menu.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class InsumoTest {

    private Lote lote(String id, double valor, String unidad, LocalDate vence) {
        return new Lote(id, new Cantidad(valor, unidad), new FechaVencimiento(vence));
    }

    @Test
    void el_stock_es_la_suma_de_los_lotes() {
        Insumo i = new Insumo("arroz", "ARROZ", "kg", 5);
        i.registrarLote(lote("l1", 30, "kg", LocalDate.now().plusDays(30)));
        i.registrarLote(lote("l2", 20, "kg", LocalDate.now().plusDays(60)));
        assertEquals(50, i.stockTotal());
        assertFalse(i.tieneStockBajo());
    }

    @Test
    void stock_en_el_minimo_o_menos_es_bajo() {
        Insumo i = new Insumo("sal", "SAL", "kg", 1);
        assertTrue(i.tieneStockBajo());
        i.registrarLote(lote("l1", 1, "kg", LocalDate.now().plusDays(10)));
        assertTrue(i.tieneStockBajo());
        i.registrarLote(lote("l2", 1, "kg", LocalDate.now().plusDays(10)));
        assertFalse(i.tieneStockBajo());
    }

    @Test
    void no_acepta_un_lote_con_otra_unidad() {
        Insumo i = new Insumo("aceite", "ACEITE", "L", 2);
        assertThrows(IllegalArgumentException.class, () -> i.registrarLote(lote("l1", 5, "kg", LocalDate.now().plusDays(5))));
    }

    @Test
    void revisar_alertas_emite_stock_bajo_y_lote_vencido() {
        Insumo i = new Insumo("pollo", "POLLO", "kg", 10);
        i.registrarLote(lote("l1", 3, "kg", LocalDate.now().minusDays(1)));
        i.revisarAlertas(LocalDate.now());
        var tipos = i.pullEventos().stream().map(e -> e.type()).toList();
        assertEquals(java.util.List.of("StockBajoEvent", "LoteVencidoEvent"), tipos);
        assertTrue(i.pullEventos().isEmpty(), "los eventos se entregan una sola vez");
    }

    @Test
    void compra_sugerida_repone_hasta_el_doble_del_minimo() {
        Insumo i = new Insumo("sal", "SAL", "kg", 5);
        i.registrarLote(lote("l1", 2.5, "kg", LocalDate.now().plusDays(10)));
        assertEquals(8, i.cantidadSugeridaDeCompra());          // ceil(10 - 2.5)
    }

    @Test
    void compra_sugerida_es_al_menos_uno() {
        Insumo sinMinimo = new Insumo("agua", "AGUA", "L", 0);
        assertEquals(1, sinMinimo.cantidadSugeridaDeCompra());
        Insumo sobrado = new Insumo("arroz", "ARROZ", "kg", 5);
        sobrado.registrarLote(lote("l1", 50, "kg", LocalDate.now().plusDays(10)));
        assertEquals(1, sobrado.cantidadSugeridaDeCompra());
    }

    @Test
    void lotes_que_vencen_hasta_incluye_el_limite_y_los_vencidos() {
        LocalDate hoy = LocalDate.of(2026, 11, 9);
        Insumo i = new Insumo("leche", "LECHE", "L", 1);
        i.registrarLote(lote("vencido", 1, "L", hoy.minusDays(1)));
        i.registrarLote(lote("en-el-limite", 1, "L", hoy.plusDays(7)));
        i.registrarLote(lote("despues", 1, "L", hoy.plusDays(8)));
        var ids = i.lotesQueVencenHasta(hoy.plusDays(7)).stream().map(Lote::loteId).toList();
        assertEquals(java.util.List.of("vencido", "en-el-limite"), ids);
    }

    @Test
    void cantidad_negativa_o_sin_unidad_es_invalida() {
        assertThrows(IllegalArgumentException.class, () -> new Cantidad(-1, "kg"));
        assertThrows(IllegalArgumentException.class, () -> new Cantidad(1, " "));
    }
}
