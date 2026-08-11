package es.delivery.manager.notificacion.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;
import es.delivery.manager.notificacion.domain.repository.NotificacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("user-1")
                .username("user")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Notificacion notificacion() {
        return Notificacion.builder()
                .id("notif-1")
                .comercioId("comercio-1")
                .tipo(TipoNotificacion.PEDIDO_PENDIENTE)
                .pedidoId("pedido-1")
                .numeroPedido("ABC234")
                .leida(false)
                .fecha(Instant.now())
                .build();
    }

    @Test
    void registrarCreaNotificacionSinLeer() {
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion creada = notificacionService.registrar(
                "comercio-1", TipoNotificacion.PEDIDO_PENDIENTE, "pedido-1", "ABC234");

        assertThat(creada.isLeida()).isFalse();
        assertThat(creada.getFecha()).isNotNull();
        assertThat(creada.getComercioId()).isEqualTo("comercio-1");
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN", "PERSONAL"})
    void usuariosDelComercioConsultanSusBandejas(TipoUsuario tipo) {
        when(notificacionRepository.findByComercioIdAndTipoAndLeida("comercio-1", TipoNotificacion.PEDIDO_PAGADO, false))
                .thenReturn(List.of(notificacion()));

        List<Notificacion> lista = notificacionService.listNoLeidas(caller(tipo), TipoNotificacion.PEDIDO_PAGADO);

        assertThat(lista).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"REPARTIDOR", "CLIENTE"})
    void otrosTiposNoConsultanNotificaciones(TipoUsuario tipo) {
        assertThatThrownBy(() -> notificacionService.listNoLeidas(caller(tipo), TipoNotificacion.PEDIDO_PENDIENTE))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void marcarLeidaSoloDelPropioComercio() {
        Notificacion ajena = notificacion();
        ajena.setComercioId("comercio-2");
        when(notificacionRepository.findById("notif-1")).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> notificacionService.marcarLeida(caller(TipoUsuario.ADMIN), "notif-1"))
                .isInstanceOf(NotificacionNotFoundException.class);

        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void marcarLeidaPersisteElCambio() {
        when(notificacionRepository.findById("notif-1")).thenReturn(Optional.of(notificacion()));
        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Notificacion leida = notificacionService.marcarLeida(caller(TipoUsuario.PERSONAL), "notif-1");

        assertThat(leida.isLeida()).isTrue();
    }
}
