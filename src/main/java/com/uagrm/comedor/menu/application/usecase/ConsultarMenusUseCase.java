package com.uagrm.comedor.menu.application.usecase;

import com.uagrm.comedor.menu.application.dto.CantidadACocinarDto;
import com.uagrm.comedor.menu.application.dto.MenuDto;
import com.uagrm.comedor.menu.domain.model.Turno;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

@Service
public class ConsultarMenusUseCase {
    private final MenuRepository repo;

    public ConsultarMenusUseCase(MenuRepository repo) { this.repo = repo; }

    /** Menús de la semana que empieza en `inicio` (o en el lunes de esta semana si no se indica). */
    public List<MenuDto> semana(Turno turno, LocalDate inicio) {
        LocalDate desde = inicio != null ? inicio : LocalDate.now().with(DayOfWeek.MONDAY);
        return repo.findBySemana(desde, turno).stream().map(MenuDto::from).toList();
    }

    /** Historial de menús (todos los turnos) entre dos fechas, ambas inclusive. */
    public List<MenuDto> historial(LocalDate desde, LocalDate hasta) {
        return repo.findByRango(desde, hasta).stream().map(MenuDto::from).toList();
    }

    /** Cuántas raciones preparar de cada plato de ese turno y día, según lo que ya está reservado. */
    public List<CantidadACocinarDto> cantidadACocinar(Turno turno, LocalDate fecha) {
        Map<String, int[]> platos = new TreeMap<>();                  // platoId -> [reservadas, cupo]
        repo.findByRango(fecha, fecha).stream().filter(m -> m.getTurno() == turno).forEach(m ->
            m.getPorciones().forEach((id, p) -> {
                int[] t = platos.computeIfAbsent(id, x -> new int[2]);
                t[0] += p.apartadas();
                t[1] += p.cupoTotal();
            }));
        return platos.entrySet().stream()
            .map(e -> CantidadACocinarDto.de(e.getKey(), e.getValue()[0], e.getValue()[1]))
            .toList();
    }

    public Optional<MenuDto> porId(String id) {
        return repo.findById(id).map(MenuDto::from);
    }
}
