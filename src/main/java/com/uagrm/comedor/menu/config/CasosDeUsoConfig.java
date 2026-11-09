package com.uagrm.comedor.menu.config;

import com.uagrm.comedor.menu.application.usecase.ApartarPorcionUseCase;
import com.uagrm.comedor.menu.application.usecase.ArmarMenuSemanalUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarInsumosUseCase;
import com.uagrm.comedor.menu.application.usecase.ConsultarMenusUseCase;
import com.uagrm.comedor.menu.application.usecase.CrearInsumoUseCase;
import com.uagrm.comedor.menu.application.usecase.LiberarPorcionUseCase;
import com.uagrm.comedor.menu.application.usecase.RegistrarLoteUseCase;
import com.uagrm.comedor.menu.domain.repository.InsumoRepository;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import com.uagrm.comedor.menu.domain.service.EventPublisher;
import com.uagrm.comedor.menu.domain.service.PlanificacionDeCocina;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra los casos de uso y servicios de dominio como beans: la capa de aplicación no conoce Spring. */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    PlanificacionDeCocina planificacionDeCocina() { return new PlanificacionDeCocina(); }

    @Bean
    ApartarPorcionUseCase apartarPorcionUseCase(MenuRepository menus, EventPublisher eventos) {
        return new ApartarPorcionUseCase(menus, eventos);
    }

    @Bean
    LiberarPorcionUseCase liberarPorcionUseCase(MenuRepository menus, EventPublisher eventos) {
        return new LiberarPorcionUseCase(menus, eventos);
    }

    @Bean
    ArmarMenuSemanalUseCase armarMenuSemanalUseCase(MenuRepository menus, EventPublisher eventos) {
        return new ArmarMenuSemanalUseCase(menus, eventos);
    }

    @Bean
    ConsultarMenusUseCase consultarMenusUseCase(MenuRepository menus, PlanificacionDeCocina planificacion) {
        return new ConsultarMenusUseCase(menus, planificacion);
    }

    @Bean
    ConsultarInsumosUseCase consultarInsumosUseCase(InsumoRepository insumos) {
        return new ConsultarInsumosUseCase(insumos);
    }

    @Bean
    CrearInsumoUseCase crearInsumoUseCase(InsumoRepository insumos) {
        return new CrearInsumoUseCase(insumos);
    }

    @Bean
    RegistrarLoteUseCase registrarLoteUseCase(InsumoRepository insumos, EventPublisher eventos) {
        return new RegistrarLoteUseCase(insumos, eventos);
    }
}
