package com.uagrm.comedor.menu.infrastructure.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servidor MCP (Model Context Protocol) por HTTP: JSON-RPC 2.0 en POST /mcp, sin estado (cada petición es completa).
 * Métodos: initialize, ping, tools/list y tools/call. Las notificaciones (sin id) se aceptan con 202.
 * Los agentes de ia-ml-service lo usan para leer menús e inventario sin tocar la base de datos.
 */
@RestController
@RequestMapping("/mcp")
public class McpController {
    static final String VERSION = "2025-06-18";
    private static final Set<String> VERSIONES = Set.of("2025-06-18", "2025-03-26", "2024-11-05");

    private final Map<String, Herramienta> herramientas;
    private final ObjectMapper json;

    public McpController(HerramientasMenu herramientas, ObjectMapper json) {
        this.herramientas = herramientas.todas();
        this.json = json;
    }

    @GetMapping
    public ResponseEntity<Void> sinStream() { return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build(); }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @SuppressWarnings("unchecked")
    public ResponseEntity<Map<String, Object>> recibir(@RequestBody Map<String, Object> msg) {
        Object id = msg.get("id");
        if (id == null) return ResponseEntity.accepted().build();                 // notificación: no lleva respuesta
        String metodo = String.valueOf(msg.get("method"));
        Map<String, Object> params = msg.get("params") instanceof Map<?, ?> p ? (Map<String, Object>) p : Map.of();
        try {
            Object resultado = switch (metodo) {
                case "initialize" -> Map.of(
                    "protocolVersion", VERSIONES.contains(String.valueOf(params.get("protocolVersion"))) ? params.get("protocolVersion") : VERSION,
                    "capabilities", Map.of("tools", Map.of("listChanged", false)),
                    "serverInfo", Map.of("name", "menu-inventario-service", "version", "1.0.0"));
                case "ping" -> Map.of();
                case "tools/list" -> Map.of("tools", herramientas.values().stream()
                    .map(h -> Map.of("name", h.nombre(), "description", h.descripcion(), "inputSchema", h.esquema())).toList());
                case "tools/call" -> llamar(params);
                default -> null;
            };
            if (resultado == null) return ResponseEntity.ok(error(id, -32601, "Método no soportado: " + metodo));
            return ResponseEntity.ok(Map.of("jsonrpc", "2.0", "id", id, "result", resultado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(error(id, -32602, e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> llamar(Map<String, Object> params) {
        Herramienta h = herramientas.get(String.valueOf(params.get("name")));
        if (h == null) throw new IllegalArgumentException("Herramienta desconocida: " + params.get("name"));
        Map<String, Object> args = params.get("arguments") instanceof Map<?, ?> a ? (Map<String, Object>) a : Map.of();
        try {
            Object r = h.accion().apply(args);
            return Map.of("content", List.of(Map.of("type", "text", "text", json.writeValueAsString(r))),
                "structuredContent", Map.of("resultado", r), "isError", false);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        } catch (RuntimeException e) {            // error al ejecutar: MCP lo devuelve como resultado con isError
            String motivo = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return Map.of("content", List.of(Map.of("type", "text", "text", motivo)), "isError", true);
        }
    }

    private static Map<String, Object> error(Object id, int codigo, String mensaje) {
        Map<String, Object> r = new HashMap<>();
        r.put("jsonrpc", "2.0");
        r.put("id", id);
        r.put("error", Map.of("code", codigo, "message", mensaje));
        return r;
    }
}
