package es.delivery.manager.notificacion.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.notificacion.application.usecase.ListNotificacionesUseCase;
import es.delivery.manager.notificacion.application.usecase.MarcarLeidaUseCase;
import es.delivery.manager.notificacion.application.usecase.RegistrarNotificacionUseCase;
import es.delivery.manager.notificacion.domain.model.Notificacion;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;
import es.delivery.manager.notificacion.domain.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService implements RegistrarNotificacionUseCase,
        ListNotificacionesUseCase, MarcarLeidaUseCase {

    private final NotificacionRepository notificacionRepository;

    @Override
    public Notificacion registrar(String comercioId, TipoNotificacion tipo, String pedidoId, String numeroPedido) {
        return notificacionRepository.save(Notificacion.builder()
                .comercioId(comercioId)
                .tipo(tipo)
                .pedidoId(pedidoId)
                .numeroPedido(numeroPedido)
                .leida(false)
                .fecha(Instant.now())
                .build());
    }

    @Override
    public List<Notificacion> listNoLeidas(TokenClaims caller, TipoNotificacion tipo) {
        checkUsuarioComercio(caller);
        return notificacionRepository.findByComercioIdAndTipoAndLeida(caller.getComercioId(), tipo, false);
    }

    @Override
    public Notificacion marcarLeida(TokenClaims caller, String notificacionId) {
        checkUsuarioComercio(caller);
        // Multi-tenant: solo notificaciones del propio comercio
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .filter(n -> n.getComercioId().equals(caller.getComercioId()))
                .orElseThrow(() -> new NotificacionNotFoundException(notificacionId));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }

    private void checkUsuarioComercio(TokenClaims caller) {
        boolean permitido = caller.getTipo() == TipoUsuario.ROOT
                || caller.getTipo() == TipoUsuario.ADMIN
                || caller.getTipo() == TipoUsuario.PERSONAL;
        if (!permitido) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede consultar las notificaciones");
        }
    }
}
