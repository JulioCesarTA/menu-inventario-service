package com.uagrm.comedor.menu.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Objeto de valor: fecha en que un lote deja de ser usable. */
public record FechaVencimiento(LocalDate fecha) {
    public boolean estaVencida(LocalDate hoy) { return !fecha.isAfter(hoy); }

    /** Vence en la fecha límite o antes (incluye los ya vencidos). */
    public boolean venceHasta(LocalDate limite) { return !fecha.isAfter(limite); }

    /** Días que faltan desde `hoy` hasta el vencimiento (negativo si ya venció). */
    public long diasRestantes(LocalDate hoy) { return ChronoUnit.DAYS.between(hoy, fecha); }
}
