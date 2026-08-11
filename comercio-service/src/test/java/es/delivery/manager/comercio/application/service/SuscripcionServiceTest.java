package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.domain.event.ComercioEvent;
import es.delivery.manager.comercio.domain.event.ComercioEventPublisher;
import es.delivery.manager.comercio.domain.event.ComercioEventType;
import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.repository.ComercioRepository;
import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuscripcionServiceTest {

    @Mock
    private ComercioRepository comercioRepository;

    @Mock
    private ComercioEventPublisher eventPublisher;

    @InjectMocks
    private SuscripcionService suscripcionService;

    private Comercio comercio(EstadoSuscripcion estado) {
        return Comercio.builder()
                .id("comercio-1")
                .nombre("Pizzeria Test")
                .cif("B12345678")
                .activo(true)
                .plan(PlanSuscripcion.MENSUAL)
                .estadoSuscripcion(estado)
                .fechaInicioSuscripcion(Instant.now().minus(20, ChronoUnit.DAYS))
                .fechaFinSuscripcion(Instant.now().plus(10, ChronoUnit.DAYS))
                .build();
    }

    @Test
    void suspenderPublicaComercioSuspendido() {
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(comercio(EstadoSuscripcion.ACTIVA)));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio suspendido = suscripcionService.suspender("comercio-1");

        assertThat(suspendido.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.SUSPENDIDA);
        ArgumentCaptor<ComercioEvent> captor = ArgumentCaptor.forClass(ComercioEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ComercioEventType.SUSPENDIDO);
    }

    @Test
    void suspenderEsIdempotenteSiYaEstabaSuspendida() {
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(comercio(EstadoSuscripcion.SUSPENDIDA)));

        Comercio resultado = suscripcionService.suspender("comercio-1");

        assertThat(resultado.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.SUSPENDIDA);
        verify(comercioRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void renovarExtiendeDesdeLaFechaFinVigente() {
        Comercio vigente = comercio(EstadoSuscripcion.ACTIVA);
        Instant finAnterior = vigente.getFechaFinSuscripcion();
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(vigente));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio renovado = suscripcionService.renovar("comercio-1", PlanSuscripcion.MENSUAL);

        assertThat(renovado.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.ACTIVA);
        assertThat(renovado.getFechaFinSuscripcion()).isAfter(finAnterior);
        // Sin suspension previa no hay evento de reactivacion
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    void renovarUnaSuspendidaPublicaComercioReactivado() {
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(comercio(EstadoSuscripcion.SUSPENDIDA)));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio renovado = suscripcionService.renovar("comercio-1", PlanSuscripcion.ANUAL);

        assertThat(renovado.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.ACTIVA);
        assertThat(renovado.getPlan()).isEqualTo(PlanSuscripcion.ANUAL);
        ArgumentCaptor<ComercioEvent> captor = ArgumentCaptor.forClass(ComercioEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(ComercioEventType.REACTIVADO);
    }

    @Test
    void renovarLanzaNotFoundSiNoExiste() {
        when(comercioRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> suscripcionService.renovar("nope", PlanSuscripcion.MENSUAL))
                .isInstanceOf(ComercioNotFoundException.class);
    }
}
