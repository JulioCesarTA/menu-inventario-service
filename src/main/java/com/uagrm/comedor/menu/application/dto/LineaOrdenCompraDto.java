package com.uagrm.comedor.menu.application.dto;

import com.uagrm.comedor.menu.domain.model.Insumo;

/** Una línea del borrador de orden de compra: se repone hasta el doble del stock mínimo. */
public record LineaOrdenCompraDto(String insumoId, String nombre, String unidad, double stockActual, double stockMinimo,
                                  int cantidadSugerida, String estado) {
    public static LineaOrdenCompraDto from(Insumo i) {
        double actual = i.stockTotal();
        int cantidad = (int) Math.max(1, Math.ceil(2 * i.getStockMinimo() - actual));
        return new LineaOrdenCompraDto(i.getId(), i.getNombre(), i.getUnidad(), actual, i.getStockMinimo(), cantidad, "BORRADOR");
    }
}
