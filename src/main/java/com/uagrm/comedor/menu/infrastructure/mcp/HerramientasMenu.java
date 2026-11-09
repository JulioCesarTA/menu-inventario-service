package com.uagrm.comedor.menu.infrastructure.mcp;

import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.domain.model.Turno;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Herramientas MCP que este microservicio ofrece a los agentes de IA (SPEC §3.1). Solo lectura. */
@Component
public class HerramientasMenu {
    private final Map<String, Herramienta> todas = new LinkedHashMap<>();

    public HerramientasMenu(ConsultarMenusUseCase menus, ConsultarInsumosUseCase insumos) {
        agregar(new Herramienta("consultar_menu", "Menús publicados de la semana actual para un turno, con cupo y porciones disponibles por plato.",
            esquema(Map.of("turno", Map.of("type", "string", "enum", List.of("DESAYUNO", "ALMUERZO", "CENA"))), List.of("turno")),
            a -> menus.semana(Turno.valueOf(texto(a, "turno").toUpperCase()), null)));
        agregar(new Herramienta("ver_stock_insumo", "Todos los insumos con su stock mínimo y sus lotes (cantidad y vencimiento).",
            esquema(Map.of(), List.of()), a -> insumos.listar()));
        agregar(new Herramienta("ver_stock_bajo", "Insumos cuyo stock total llegó al mínimo o quedó por debajo.",
            esquema(Map.of(), List.of()), a -> insumos.stockBajo()));
        agregar(new Herramienta("lotes_por_vencer", "Lotes que vencen dentro de los próximos `dias` días (o ya vencidos), del más urgente al menos.",
            esquema(Map.of("dias", Map.of("type", "integer", "minimum", 0, "maximum", 365)), List.of()),
            a -> insumos.porVencer(entero(a, "dias", 7), LocalDate.now())));
    }

    private void agregar(Herramienta h) { todas.put(h.nombre(), h); }

    public Map<String, Herramienta> todas() { return todas; }

    static Map<String, Object> esquema(Map<String, Object> propiedades, List<String> requeridas) {
        return Map.of("type", "object", "properties", propiedades, "required", requeridas);
    }

    static String texto(Map<String, Object> a, String campo) {
        Object v = a.get(campo);
        if (v == null || v.toString().isBlank()) throw new IllegalArgumentException("Falta el argumento «" + campo + "»");
        return v.toString();
    }

    static int entero(Map<String, Object> a, String campo, int porDefecto) {
        Object v = a.get(campo);
        if (v == null) return porDefecto;
        if (v instanceof Number n) return n.intValue();
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { throw new IllegalArgumentException("«" + campo + "» debe ser un número entero"); }
    }
}
