package es.delivery.manager.comercio.domain.event;

import es.delivery.manager.comercio.domain.model.Comercio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComercioEvent {
    private ComercioEventType type;
    private Comercio comercio;
    private Instant timestamp;
}
