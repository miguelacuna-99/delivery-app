package es.delivery.manager.pago.application.service;

import es.delivery.manager.contracts.event.RoutingKeys;
import es.delivery.manager.contracts.model.EstadoPago;
import es.delivery.manager.pago.application.usecase.ProcesarDevolucionUseCase;
import es.delivery.manager.pago.application.usecase.ProcesarPagoUseCase;
import es.delivery.manager.pago.domain.event.PagoEvent;
import es.delivery.manager.pago.domain.event.PagoEventPublisher;
import es.delivery.manager.pago.domain.model.Pago;
import es.delivery.manager.pago.domain.repository.PagoRepository;
import es.delivery.manager.pago.domain.service.PasarelaPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoService implements ProcesarPagoUseCase, ProcesarDevolucionUseCase {

    private final PagoRepository pagoRepository;
    private final PasarelaPort pasarelaPort;
    private final PagoEventPublisher eventPublisher;

    @Override
    public Pago procesarPago(String pedidoId, String comercioId, String clienteId, BigDecimal importe) {
        // Idempotencia: un reenvio del evento no cobra dos veces
        Optional<Pago> existente = pagoRepository.findByPedidoId(pedidoId);
        if (existente.isPresent()) {
            log.warn("pago.solicitado duplicado para pedido {}: se ignora", pedidoId);
            return existente.get();
        }

        PasarelaPort.ResultadoCobro resultado = pasarelaPort.cobrar(pedidoId, importe);
        Pago pago = Pago.builder()
                .pedidoId(pedidoId)
                .comercioId(comercioId)
                .clienteId(clienteId)
                .importe(importe)
                .firma(resultado.firma())
                .estado(resultado.autorizado() ? EstadoPago.COMPLETADO : EstadoPago.FALLIDO)
                .fecha(Instant.now())
                .build();
        Pago saved = pagoRepository.save(pago);

        publish(saved, resultado.autorizado() ? RoutingKeys.PAGO_COMPLETADO : RoutingKeys.PAGO_FALLIDO);
        return saved;
    }

    @Override
    public Pago procesarDevolucion(String pedidoId) {
        Pago pago = pagoRepository.findByPedidoId(pedidoId).orElse(null);
        if (pago == null) {
            log.warn("devolucion.solicitada sin pago para pedido {}: se ignora", pedidoId);
            return null;
        }
        if (pago.getEstado() != EstadoPago.COMPLETADO) {
            log.warn("devolucion.solicitada para pago en estado {}: se ignora", pago.getEstado());
            return pago;
        }

        pago.setEstado(EstadoPago.DEVUELTO);
        pago.setFechaDevolucion(Instant.now());
        Pago saved = pagoRepository.save(pago);

        publish(saved, RoutingKeys.DEVOLUCION_COMPLETADA);
        return saved;
    }

    private void publish(Pago pago, String routingKey) {
        eventPublisher.publish(PagoEvent.builder()
                .routingKey(routingKey)
                .pago(pago)
                .timestamp(Instant.now())
                .build());
    }
}
