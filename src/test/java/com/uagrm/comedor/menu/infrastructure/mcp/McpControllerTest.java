package com.uagrm.comedor.menu.infrastructure.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.domain.model.*;
import com.uagrm.comedor.menu.domain.repository.InsumoRepository;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class McpControllerTest {

    private McpController mcp;

    @BeforeEach
    void preparar() {
        Insumo arroz = new Insumo("arroz", "ARROZ", "kg", 5);
        arroz.registrarLote(new Lote("l1", new Cantidad(50, "kg"), new FechaVencimiento(LocalDate.now().plusDays(2))));
        Insumo sal = new Insumo("sal", "SAL", "kg", 1);
        InsumoRepository insumos = new InsumoRepository() {
            public Insumo save(Insumo i) { return i; }
            public Optional<Insumo> findById(String id) { return Optional.empty(); }
            public List<Insumo> findAll() { return List.of(arroz, sal); }
        };
        MenuRepository menus = new MenuRepository() {
            public Menu save(Menu m) { return m; }
            public Optional<Menu> findById(String id) { return Optional.empty(); }
            public List<Menu> findBySemana(LocalDate inicio, Turno t) {
                Menu m = new Menu("m1", LocalDate.now(), t);
                m.agregarPlato("plato-pollo", 100);
                return List.of(m);
            }
            public List<Menu> findByRango(LocalDate d, LocalDate h) { return List.of(); }
            public Optional<Menu> findByFechaYTurno(LocalDate f, Turno t) { return Optional.empty(); }
        };
        ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
        mcp = new McpController(new HerramientasMenu(new ConsultarMenusUseCase(menus), new ConsultarInsumosUseCase(insumos)), json);
    }

    private Map<String, Object> pedir(String metodo, Map<String, Object> params) {
        Map<String, Object> msg = new HashMap<>(Map.of("jsonrpc", "2.0", "id", 1, "method", metodo));
        if (params != null) msg.put("params", params);
        return mcp.recibir(msg).getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resultado(Map<String, Object> r) { return (Map<String, Object>) r.get("result"); }

    @Test
    void initialize_responde_la_version_y_que_ofrece_herramientas() {
        var r = resultado(pedir("initialize", Map.of("protocolVersion", "2025-06-18", "capabilities", Map.of(),
            "clientInfo", Map.of("name", "test", "version", "1"))));
        assertEquals("2025-06-18", r.get("protocolVersion"));
        assertTrue(((Map<?, ?>) r.get("capabilities")).containsKey("tools"));
    }

    @Test
    void una_notificacion_no_lleva_respuesta() {
        var resp = mcp.recibir(Map.of("jsonrpc", "2.0", "method", "notifications/initialized"));
        assertEquals(202, resp.getStatusCode().value());
    }

    @Test
    @SuppressWarnings("unchecked")
    void tools_list_trae_las_herramientas_con_su_esquema() {
        var tools = (List<Map<String, Object>>) resultado(pedir("tools/list", null)).get("tools");
        var nombres = tools.stream().map(t -> t.get("name")).toList();
        assertEquals(List.of("consultar_menu", "ver_stock_insumo", "ver_stock_bajo", "lotes_por_vencer"), nombres);
        assertEquals("object", ((Map<?, ?>) tools.get(0).get("inputSchema")).get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void tools_call_devuelve_el_resultado_estructurado() {
        var r = resultado(pedir("tools/call", Map.of("name", "lotes_por_vencer", "arguments", Map.of("dias", 7))));
        assertEquals(false, r.get("isError"));
        var lotes = (List<?>) ((Map<String, Object>) r.get("structuredContent")).get("resultado");
        assertEquals(1, lotes.size());
        var bajo = resultado(pedir("tools/call", Map.of("name", "ver_stock_bajo", "arguments", Map.of())));
        assertEquals(1, ((List<?>) ((Map<String, Object>) bajo.get("structuredContent")).get("resultado")).size());
    }

    @Test
    void un_argumento_que_falta_es_un_error_de_la_herramienta() {
        var r = resultado(pedir("tools/call", Map.of("name", "consultar_menu", "arguments", Map.of())));
        assertEquals(true, r.get("isError"));
    }

    @Test
    void herramienta_o_metodo_desconocido_es_error_jsonrpc() {
        assertEquals(-32602, ((Map<?, ?>) pedir("tools/call", Map.of("name", "no_existe")).get("error")).get("code"));
        assertEquals(-32601, ((Map<?, ?>) pedir("resources/list", null).get("error")).get("code"));
    }
}
