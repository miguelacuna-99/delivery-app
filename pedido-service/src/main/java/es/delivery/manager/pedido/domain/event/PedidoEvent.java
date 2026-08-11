package es.delivery.manager.pedido.domain.event;

import es.delivery.manager.pedido.domain.model.Pedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoEvent {
    private EventType type;
    private Pedido pedido;
    private Instant timestamp;
}
