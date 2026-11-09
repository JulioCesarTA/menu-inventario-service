package com.uagrm.comedor.menu.infrastructure.graphql;

import com.uagrm.comedor.menu.application.dto.InsumoDto;
import com.uagrm.comedor.menu.application.dto.MenuDto;
import com.uagrm.comedor.menu.application.usecase.ArmarMenuSemanalUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.application.usecase.CrearInsumoUseCase;
import com.uagrm.comedor.menu.application.usecase.RegistrarLoteUseCase;
import com.uagrm.comedor.menu.domain.model.Turno;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.util.List;

/** Subgrafo GraphQL de menú e inventario: llama a los mismos casos de uso que el API REST, sin pasar por él. */
@Controller
public class MenuGraphqlController {

    public record PorcionVista(String platoId, int cupo, int apartadas, int disponibles) {}
    public record LoteVista(String loteId, double cantidad, String unidad, String vence) {}
    public record RecepcionLote(boolean ok, String insumoId, String loteId) {}

    private final ConsultarMenusUseCase menus;
    private final ArmarMenuSemanalUseCase armar;
    private final ConsultarInsumosUseCase insumos;
    private final CrearInsumoUseCase crearInsumo;
    private final RegistrarLoteUseCase registrarLote;

    public MenuGraphqlController(ConsultarMenusUseCase menus, ArmarMenuSemanalUseCase armar, ConsultarInsumosUseCase insumos,
                                 CrearInsumoUseCase crearInsumo, RegistrarLoteUseCase registrarLote) {
        this.menus = menus;
        this.armar = armar;
        this.insumos = insumos;
        this.crearInsumo = crearInsumo;
        this.registrarLote = registrarLote;
    }

    private static Turno turno(String t) {
        try {
            return Turno.valueOf(t.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Turno desconocido: " + t);
        }
    }

    @QueryMapping
    public List<MenuDto> menusSemana(@Argument String turno) {
        return menus.semana(turno(turno), null);
    }

    @QueryMapping(name = "insumos")
    public List<InsumoDto> listarInsumos() {
        return insumos.listar();
    }

    @SchemaMapping(typeName = "Menu", field = "porciones")
    public List<PorcionVista> porciones(MenuDto m) {
        return m.porciones().entrySet().stream()
            .map(e -> new PorcionVista(e.getKey(), e.getValue().cupo(), e.getValue().apartadas(), e.getValue().disponibles()))
            .toList();
    }

    @SchemaMapping(typeName = "Insumo", field = "lotes")
    public List<LoteVista> lotes(InsumoDto i) {
        return i.lotes().stream()
            .map(l -> new LoteVista(l.loteId(), l.cantidad().valor(), l.cantidad().unidad(), l.vencimiento().fecha().toString()))
            .toList();
    }

    @SchemaMapping(typeName = "Insumo", field = "stockTotal")
    public double stockTotal(InsumoDto i) {
        return i.lotes().stream().mapToDouble(l -> l.cantidad().valor()).sum();
    }

    /** Federation: otro subgrafo puede referirse a un menú o a un insumo por su id. */
    @EntityMapping
    public MenuDto menu(@Argument String id) {
        return menus.porId(id).orElse(null);
    }

    @EntityMapping
    public InsumoDto insumo(@Argument String id) {
        return insumos.listar().stream().filter(i -> i.id().equals(id)).findFirst().orElse(null);
    }

    @MutationMapping
    public MenuDto publicarMenu(@Argument String fecha, @Argument String turno, @Argument List<ArmarMenuSemanalUseCase.Entrada.PlatoCupo> platos) {
        return armar.ejecutar(new ArmarMenuSemanalUseCase.Entrada(LocalDate.parse(fecha), turno(turno), platos));
    }

    @MutationMapping(name = "crearInsumo")
    public InsumoDto crear(@Argument String id, @Argument String nombre, @Argument String unidad, @Argument double stockMinimo) {
        return crearInsumo.ejecutar(new CrearInsumoUseCase.Entrada(id, nombre, unidad, stockMinimo));
    }

    @MutationMapping(name = "registrarLote")
    public RecepcionLote recibir(@Argument String insumoId, @Argument String loteId, @Argument double cantidad,
                                 @Argument String unidad, @Argument String vence) {
        registrarLote.ejecutar(new RegistrarLoteUseCase.Entrada(insumoId, loteId, cantidad, unidad, LocalDate.parse(vence)));
        return new RecepcionLote(true, insumoId, loteId);
    }
}
