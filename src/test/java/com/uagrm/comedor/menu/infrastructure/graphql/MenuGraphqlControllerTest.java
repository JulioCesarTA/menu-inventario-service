package com.uagrm.comedor.menu.infrastructure.graphql;

import com.uagrm.comedor.menu.application.dto.InsumoDto;
import com.uagrm.comedor.menu.application.dto.MenuDto;
import com.uagrm.comedor.menu.application.usecase.ArmarMenuSemanalUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.application.usecase.CrearInsumoUseCase;
import com.uagrm.comedor.menu.application.usecase.RegistrarLoteUseCase;
import com.uagrm.comedor.menu.domain.model.Turno;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** El subgrafo con el esquema real y casos de uso falsos: lo que el gateway de Apollo le pregunta. */
@GraphQlTest(MenuGraphqlController.class)
@Import(FederacionConfig.class)
class MenuGraphqlControllerTest {

    @Autowired GraphQlTester graphql;
    @MockBean ConsultarMenusUseCase menus;
    @MockBean ArmarMenuSemanalUseCase armar;
    @MockBean ConsultarInsumosUseCase insumos;
    @MockBean CrearInsumoUseCase crearInsumo;
    @MockBean RegistrarLoteUseCase registrarLote;

    private final MenuDto menu = new MenuDto("m1", LocalDate.parse("2026-11-10"), Turno.ALMUERZO, true,
        Map.of("plato-pollo", new MenuDto.PorcionDto(100, 30, 70)));
    private final InsumoDto arroz = new InsumoDto("arroz", "ARROZ", "kg", 5, List.of(
        new InsumoDto.LoteDto("l1", new InsumoDto.CantidadDto(50, "kg"), new InsumoDto.VencimientoDto(LocalDate.parse("2026-12-01")))));

    @Test
    void publica_su_esquema_para_que_el_gateway_lo_componga() {
        String sdl = graphql.document("{ _service { sdl } }").execute().path("_service.sdl").entity(String.class).get();
        assertTrue(sdl.contains("federation/v2"), sdl);
        assertTrue(sdl.contains("type Insumo"), sdl);
    }

    @Test
    void menus_de_la_semana_con_sus_porciones_como_lista() {
        when(menus.semana(Turno.ALMUERZO, null)).thenReturn(List.of(menu));
        graphql.document("{ menusSemana(turno: \"ALMUERZO\") { id fecha turno porciones { platoId cupo disponibles } } }").execute()
            .path("menusSemana[0].fecha").entity(String.class).isEqualTo("2026-11-10")
            .path("menusSemana[0].turno").entity(String.class).isEqualTo("ALMUERZO")
            .path("menusSemana[0].porciones[0].platoId").entity(String.class).isEqualTo("plato-pollo")
            .path("menusSemana[0].porciones[0].disponibles").entity(Integer.class).isEqualTo(70);
    }

    @Test
    void insumos_con_stock_total_y_lotes() {
        when(insumos.listar()).thenReturn(List.of(arroz));
        graphql.document("{ insumos { id stockTotal lotes { loteId cantidad vence } } }").execute()
            .path("insumos[0].stockTotal").entity(Double.class).isEqualTo(50.0)
            .path("insumos[0].lotes[0].cantidad").entity(Double.class).isEqualTo(50.0)
            .path("insumos[0].lotes[0].vence").entity(String.class).isEqualTo("2026-12-01");
    }

    @Test
    void otro_subgrafo_puede_pedir_un_menu_como_entidad() {
        when(menus.porId("m1")).thenReturn(Optional.of(menu));
        graphql.document("query($r: [_Any!]!) { _entities(representations: $r) { ... on Menu { turno } } }")
            .variable("r", List.of(Map.of("__typename", "Menu", "id", "m1"))).execute()
            .path("_entities[0].turno").entity(String.class).isEqualTo("ALMUERZO");
    }

    @Test
    void publicar_menu_llama_al_caso_de_uso() {
        when(armar.ejecutar(any())).thenReturn(menu);
        graphql.document("mutation { publicarMenu(fecha: \"2026-11-10\", turno: \"almuerzo\", platos: [{ platoId: \"plato-pollo\", cupo: 100 }]) { id } }").execute()
            .path("publicarMenu.id").entity(String.class).isEqualTo("m1");
        verify(armar).ejecutar(new ArmarMenuSemanalUseCase.Entrada(LocalDate.parse("2026-11-10"), Turno.ALMUERZO,
            List.of(new ArmarMenuSemanalUseCase.Entrada.PlatoCupo("plato-pollo", 100))));
    }

    @Test
    void un_error_de_negocio_llega_con_su_motivo() {
        doThrow(new IllegalArgumentException("Insumo no existe: x")).when(registrarLote).ejecutar(any());
        graphql.document("mutation { registrarLote(insumoId: \"x\", loteId: \"l\", cantidad: 1, unidad: \"kg\", vence: \"2026-12-01\") { ok } }").execute()
            .errors().satisfy(e -> assertTrue(e.get(0).getMessage().contains("Insumo no existe")));
    }
}
