package com.uagrm.comedor.menu.application.usecase;

import com.uagrm.comedor.menu.application.dto.InsumoDto;
import com.uagrm.comedor.menu.application.dto.LineaOrdenCompraDto;
import com.uagrm.comedor.menu.application.dto.LotePorVencerDto;
import com.uagrm.comedor.menu.domain.model.Insumo;
import com.uagrm.comedor.menu.domain.repository.InsumoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class ConsultarInsumosUseCase {
    private final InsumoRepository repo;

    public ConsultarInsumosUseCase(InsumoRepository repo) { this.repo = repo; }

    public List<InsumoDto> listar() {
        return repo.findAll().stream().map(InsumoDto::from).toList();
    }

    /** Insumos cuyo stock total llegó al mínimo o quedó por debajo. */
    public List<InsumoDto> stockBajo() {
        return repo.findAll().stream().filter(Insumo::tieneStockBajo).map(InsumoDto::from).toList();
    }

    /** Borrador de orden de compra con los insumos de stock bajo. No compra ni guarda nada: lo decide el administrador. */
    public List<LineaOrdenCompraDto> ordenCompraBorrador() {
        return repo.findAll().stream().filter(Insumo::tieneStockBajo).map(LineaOrdenCompraDto::from).toList();
    }

    /** Lotes que vencen dentro de `dias` días (o ya vencidos), del más urgente al menos urgente. */
    public List<LotePorVencerDto> porVencer(int dias, LocalDate hoy) {
        LocalDate limite = hoy.plusDays(dias);
        return repo.findAll().stream()
            .flatMap(i -> i.getLotes().stream()
                .filter(l -> !l.vencimiento().fecha().isAfter(limite))
                .map(l -> new LotePorVencerDto(i.getId(), i.getNombre(), l.cantidad().valor(), i.getUnidad(),
                    l.vencimiento().fecha(), ChronoUnit.DAYS.between(hoy, l.vencimiento().fecha()))))
            .sorted(Comparator.comparingLong(LotePorVencerDto::diasRestantes))
            .toList();
    }
}
