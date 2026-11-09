package com.uagrm.comedor.menu.application.usecase;

import com.uagrm.comedor.menu.application.dto.CantidadACocinarDto;
import com.uagrm.comedor.menu.application.dto.MenuDto;
import com.uagrm.comedor.menu.domain.model.Menu;
import com.uagrm.comedor.menu.domain.model.Turno;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import com.uagrm.comedor.menu.domain.service.PlanificacionDeCocina;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ConsultarMenusUseCase {
    private final MenuRepository repo;
    private final PlanificacionDeCocina planificacion;

    public ConsultarMenusUseCase(MenuRepository repo, PlanificacionDeCocina planificacion) {
        this.repo = repo; this.planificacion = planificacion;
    }

    public ConsultarMenusUseCase(MenuRepository repo) { this(repo, new PlanificacionDeCocina()); }

    /** Menús de la semana que empieza en `inicio` (o en el lunes de esta semana si no se indica). */
    public List<MenuDto> semana(Turno turno, LocalDate inicio) {
        LocalDate desde = inicio != null ? inicio : Menu.inicioDeSemana(LocalDate.now());
        return repo.findBySemana(desde, turno).stream().map(MenuDto::from).toList();
    }

    /** Historial de menús (todos los turnos) entre dos fechas, ambas inclusive. */
    public List<MenuDto> historial(LocalDate desde, LocalDate hasta) {
        return repo.findByRango(desde, hasta).stream().map(MenuDto::from).toList();
    }

    /** Cuántas raciones preparar de cada plato de ese turno y día, según lo que ya está reservado. */
    public List<CantidadACocinarDto> cantidadACocinar(Turno turno, LocalDate fecha) {
        return planificacion.porcionesDelTurno(repo.findByRango(fecha, fecha), turno).values().stream()
            .map(CantidadACocinarDto::from)
            .toList();
    }

    public Optional<MenuDto> porId(String id) {
        return repo.findById(id).map(MenuDto::from);
    }
}
