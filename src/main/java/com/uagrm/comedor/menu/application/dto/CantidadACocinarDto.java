package com.uagrm.comedor.menu.application.dto;

/** Raciones a preparar de un plato: lo reservado más un 10 % de margen, sin pasar del cupo. */
public record CantidadACocinarDto(String platoId, int reservadas, int cupo, int sugerida) {
    public static CantidadACocinarDto de(String platoId, int reservadas, int cupo) {
        int margen = (reservadas + 9) / 10;      // 10 % redondeado hacia arriba
        return new CantidadACocinarDto(platoId, reservadas, cupo, Math.min(cupo, reservadas + margen));
    }
}
