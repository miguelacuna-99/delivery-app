package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.domain.model.EstadoComercio;
import es.delivery.manager.pedido.domain.repository.EstadoComercioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EstadoComercioServiceTest {

    @Mock
    private EstadoComercioRepository estadoComercioRepository;

    @InjectMocks
    private EstadoComercioService estadoComercioService;

    @Test
    void suspenderGuardaElComercioComoNoOperativo() {
        when(estadoComercioRepository.findByComercioId("comercio-1")).thenReturn(Optional.empty());

        estadoComercioService.marcarSuspendido("comercio-1");

        ArgumentCaptor<EstadoComercio> captor = ArgumentCaptor.forClass(EstadoComercio.class);
        verify(estadoComercioRepository).save(captor.capture());
        assertThat(captor.getValue().isOperativo()).isFalse();
        assertThat(captor.getValue().getComercioId()).isEqualTo("comercio-1");
        assertThat(captor.getValue().getFechaActualizacion()).isNotNull();
    }

    @Test
    void reactivarSobrescribeElEstadoAnterior() {
        when(estadoComercioRepository.findByComercioId("comercio-1")).thenReturn(Optional.of(
                EstadoComercio.builder().id("estado-1").comercioId("comercio-1").operativo(false).build()));

        estadoComercioService.marcarOperativo("comercio-1");

        ArgumentCaptor<EstadoComercio> captor = ArgumentCaptor.forClass(EstadoComercio.class);
        verify(estadoComercioRepository).save(captor.capture());
        assertThat(captor.getValue().isOperativo()).isTrue();
        // Reutiliza el registro existente en vez de crear otro
        assertThat(captor.getValue().getId()).isEqualTo("estado-1");
    }
}
