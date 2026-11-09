package com.uagrm.comedor.menu.infrastructure.mcp;

import java.util.Map;
import java.util.function.Function;

/** Una herramienta MCP: nombre, descripción, esquema JSON de sus argumentos y lo que hace. */
public record Herramienta(String nombre, String descripcion, Map<String, Object> esquema,
                          Function<Map<String, Object>, Object> accion) {}
