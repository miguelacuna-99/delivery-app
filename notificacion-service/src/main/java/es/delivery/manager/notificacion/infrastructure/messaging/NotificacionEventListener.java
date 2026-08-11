package es.delivery.manager.notificacion.infrastructure.messaging;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.notificacion.application.usecase.RegistrarNotificacionUseCase;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.infrastructure.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * pedido.creado -> bandeja PEDIDOS PENDIENTES; pago.completado -> PEDIDOS PAGADOS.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificacionEventListener {

    private final RegistrarNotificacionUseCase registrarNotificacionUseCase;

    @RabbitListener(queues = RabbitConfig.PEDIDO_CREADO_QUEUE)
    public void onPedidoCreado(PedidoEventMessage message) {
        log.debug("Recibido pedido.creado para pedido {}", message.getPedidoId());
        registrarNotificacionUseCase.registrar(
                message.getComercioId(),
                TipoNotificacion.PEDIDO_PENDIENTE,
                message.getPedidoId(),
                message.getNumeroPedido());
    }

    @RabbitListener(queues = RabbitConfig.PAGO_COMPLETADO_QUEUE)
    public void onPagoCompletado(PagoEventMessage message) {
        log.debug("Recibido pago.completado para pedido {}", message.getPedidoId());
        // pago.completado no lleva numeroPedido (contrato de pago): la bandeja
        // de pagados referencia el pedido por su id
        registrarNotificacionUseCase.registrar(
                message.getComercioId(),
                TipoNotificacion.PEDIDO_PAGADO,
                message.getPedidoId(),
                null);
    }
}
