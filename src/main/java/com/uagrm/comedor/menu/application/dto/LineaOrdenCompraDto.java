package com.uagrm.comedor.menu.application.dto;

import com.uagrm.comedor.menu.domain.model.Insumo;

/** Una línea del borrador de orden de compra (la cantidad la decide Insumo.cantidadSugeridaDeCompra()). */
public record LineaOrdenCompraDto(String insumoId, String nombre, String unidad, double stockActual, double stockMinimo,
                                  int cantidadSugerida, String estado) {
    public static LineaOrdenCompraDto from(Insumo i) {
        return new LineaOrdenCompraDto(i.getId(), i.getNombre(), i.getUnidad(), i.stockTotal(), i.getStockMinimo(),
            i.cantidadSugeridaDeCompra(), "BORRADOR");
    }
}
