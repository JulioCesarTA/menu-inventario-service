package com.uagrm.comedor.menu.application.dto;

import java.time.LocalDate;

/** Un lote que vence pronto (lo usa el agente de inventario para no perder insumos). */
public record LotePorVencerDto(String insumoId, String nombre, double cantidad, String unidad, LocalDate vence, long diasRestantes) {}
