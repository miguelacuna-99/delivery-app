package es.delivery.manager.notificacion.infrastructure.repository;

import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificaciones")
public class NotificacionDocument {
    @Id
    private String id;
    @Indexed
    private String comercioId;
    private TipoNotificacion tipo;
    private String pedidoId;
    private String numeroPedido;
    private boolean leida;
    private Instant fecha;
}
