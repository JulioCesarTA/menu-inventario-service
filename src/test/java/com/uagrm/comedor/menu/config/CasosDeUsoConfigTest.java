package com.uagrm.comedor.menu.config;

import com.uagrm.comedor.menu.application.usecase.*;
import com.uagrm.comedor.menu.domain.repository.InsumoRepository;
import com.uagrm.comedor.menu.domain.repository.MenuRepository;
import com.uagrm.comedor.menu.domain.service.EventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Los casos de uso ya no llevan @Service: se registran como beans en CasosDeUsoConfig. */
class CasosDeUsoConfigTest {

    @Test
    void registra_los_siete_casos_de_uso() {
        new ApplicationContextRunner()
            .withBean(MenuRepository.class, () -> mock(MenuRepository.class))
            .withBean(InsumoRepository.class, () -> mock(InsumoRepository.class))
            .withBean(EventPublisher.class, () -> mock(EventPublisher.class))
            .withUserConfiguration(CasosDeUsoConfig.class)
            .run(ctx -> assertThat(ctx)
                .hasNotFailed()
                .hasSingleBean(ApartarPorcionUseCase.class)
                .hasSingleBean(LiberarPorcionUseCase.class)
                .hasSingleBean(ArmarMenuSemanalUseCase.class)
                .hasSingleBean(ConsultarMenusUseCase.class)
                .hasSingleBean(ConsultarInsumosUseCase.class)
                .hasSingleBean(CrearInsumoUseCase.class)
                .hasSingleBean(RegistrarLoteUseCase.class));
    }
}
