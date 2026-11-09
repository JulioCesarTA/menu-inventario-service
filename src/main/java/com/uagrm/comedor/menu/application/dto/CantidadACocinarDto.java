package com.uagrm.comedor.menu.application.dto;

import com.uagrm.comedor.menu.domain.model.Porcion;

/** Raciones a preparar de un plato (la regla del margen vive en Porcion.racionesACocinar()). */
public record CantidadACocinarDto(String platoId, int reservadas, int cupo, int sugerida) {
    public static CantidadACocinarDto from(Porcion p) {
        return new CantidadACocinarDto(p.platoId(), p.apartadas(), p.cupoTotal(), p.racionesACocinar());
    }
}
