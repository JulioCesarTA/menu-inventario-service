package com.uagrm.comedor.menu.infrastructure.mcp;

import com.uagrm.comedor.menu.application.usecase.ArmarMenuSemanalUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.domain.model.Turno;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Herramientas MCP que este microservicio ofrece a los agentes de IA (SPEC §3.1).
 * Solo publicar_menu cambia datos, y los agentes la usan únicamente después de que el administrador aprueba la propuesta.
 */
@Component
public class HerramientasMenu {
    private final Map<String, Herramienta> todas = new LinkedHashMap<>();

    public HerramientasMenu(ConsultarMenusUseCase menus, ConsultarInsumosUseCase insumos, ArmarMenuSemanalUseCase armar) {
        agregar(new Herramienta("consultar_menu", "Menús publicados de la semana actual para un turno, con cupo y porciones disponibles por plato.",
            esquema(Map.of("turno", TURNO), List.of("turno")),
            a -> menus.semana(turno(a), null)));
        agregar(new Herramienta("ver_stock_insumo", "Todos los insumos con su stock mínimo y sus lotes (cantidad y vencimiento).",
            esquema(Map.of(), List.of()), a -> insumos.listar()));
        agregar(new Herramienta("ver_stock_bajo", "Insumos cuyo stock total llegó al mínimo o quedó por debajo.",
            esquema(Map.of(), List.of()), a -> insumos.stockBajo()));
        agregar(new Herramienta("lotes_por_vencer", "Lotes que vencen dentro de los próximos `dias` días (o ya vencidos), del más urgente al menos.",
            esquema(Map.of("dias", Map.of("type", "integer", "minimum", 0, "maximum", 365)), List.of()),
            a -> insumos.porVencer(entero(a, "dias", 7), LocalDate.now())));
        agregar(new Herramienta("sugerir_cantidad_cocinar", "Raciones a preparar de cada plato de un turno y día (hoy por defecto): lo reservado más 10 % de margen, sin pasar del cupo.",
            esquema(Map.of("turno", TURNO, "fecha", Map.of("type", "string", "format", "date")), List.of("turno")),
            a -> menus.cantidadACocinar(turno(a), a.get("fecha") == null ? LocalDate.now() : LocalDate.parse(texto(a, "fecha")))));
        agregar(new Herramienta("crear_orden_compra_borrador", "Borrador de orden de compra con los insumos de stock bajo. No compra ni guarda nada: lo aprueba el administrador.",
            esquema(Map.of(), List.of()), a -> insumos.ordenCompraBorrador()));
        agregar(new Herramienta("publicar_menu", "Publica el menú de un día y turno con sus platos y cupos. Usar solo con aprobación del administrador.",
            esquema(Map.of("fecha", Map.of("type", "string", "format", "date"), "turno", TURNO,
                "platos", Map.of("type", "array", "minItems", 1, "items", Map.of("type", "object",
                    "properties", Map.of("platoId", Map.of("type", "string"), "cupo", Map.of("type", "integer", "minimum", 1)),
                    "required", List.of("platoId", "cupo")))), List.of("fecha", "turno", "platos")),
            a -> armar.ejecutar(new ArmarMenuSemanalUseCase.Entrada(LocalDate.parse(texto(a, "fecha")), turno(a), platos(a)))));
    }

    private static final Map<String, Object> TURNO = Map.of("type", "string", "enum", List.of("DESAYUNO", "ALMUERZO", "CENA"));

    private static Turno turno(Map<String, Object> a) { return Turno.valueOf(texto(a, "turno").toUpperCase()); }

    @SuppressWarnings("unchecked")
    private static List<ArmarMenuSemanalUseCase.Entrada.PlatoCupo> platos(Map<String, Object> a) {
        if (!(a.get("platos") instanceof List<?> lista) || lista.isEmpty()) throw new IllegalArgumentException("Falta el argumento «platos»");
        return lista.stream().map(p -> {
            Map<String, Object> m = (Map<String, Object>) p;
            return new ArmarMenuSemanalUseCase.Entrada.PlatoCupo(texto(m, "platoId"), entero(m, "cupo", 0));
        }).toList();
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
