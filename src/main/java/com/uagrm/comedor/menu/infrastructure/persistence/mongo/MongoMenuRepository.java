package com.uagrm.comedor.menu.infrastructure.persistence.mongo;

import com.uagrm.comedor.menu.domain.model.Menu;
import com.uagrm.comedor.menu.domain.model.Turno;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface SpringMenuRepo extends MongoRepository<MenuDocument, String> {
    // `Between` de Spring Data Mongo excluye los extremos: se usan comparaciones inclusivas para no perder el primer ni el último día
    List<MenuDocument> findByFechaGreaterThanEqualAndFechaLessThanEqualAndTurno(LocalDate from, LocalDate to, Turno turno);
    Optional<MenuDocument> findByFechaAndTurno(LocalDate fecha, Turno turno);
    List<MenuDocument> findByFechaGreaterThanEqualAndFechaLessThanEqual(LocalDate from, LocalDate to);
}

/** Adaptador de salida: implementa el puerto MenuRepository del dominio sobre Mongo. */
@Repository
public class MongoMenuRepository implements MenuRepository {
    private final SpringMenuRepo repo;
    public MongoMenuRepository(SpringMenuRepo r) { this.repo = r; }

    @Override public Menu save(Menu m) { return repo.save(MenuDocument.desde(m)).aDominio(); }
    @Override public Optional<Menu> findById(String id) { return repo.findById(id).map(MenuDocument::aDominio); }
    @Override public List<Menu> findBySemana(LocalDate inicio, Turno turno) {
        return repo.findByFechaGreaterThanEqualAndFechaLessThanEqualAndTurno(inicio, inicio.plusDays(6), turno).stream().map(MenuDocument::aDominio).toList();
    }
    @Override public List<Menu> findByRango(LocalDate desde, LocalDate hasta) {
        return repo.findByFechaGreaterThanEqualAndFechaLessThanEqual(desde, hasta).stream().map(MenuDocument::aDominio).toList();
    }
    @Override public Optional<Menu> findByFechaYTurno(LocalDate f, Turno t) {
        return repo.findByFechaAndTurno(f, t).map(MenuDocument::aDominio);
    }
}
