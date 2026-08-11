package es.delivery.manager.notificacion.infrastructure.controller.dto;

import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private String id;
    private String comercioId;
    private TipoNotificacion tipo;
    private String pedidoId;
    private String numeroPedido;
    private boolean leida;
    private Instant fecha;
}
