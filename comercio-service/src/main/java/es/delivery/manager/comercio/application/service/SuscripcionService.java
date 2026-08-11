package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.application.usecase.RenovarSuscripcionUseCase;
import es.delivery.manager.comercio.application.usecase.SuspenderSuscripcionUseCase;
import es.delivery.manager.comercio.domain.event.ComercioEvent;
import es.delivery.manager.comercio.domain.event.ComercioEventPublisher;
import es.delivery.manager.comercio.domain.event.ComercioEventType;
import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.repository.ComercioRepository;
import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SuscripcionService implements SuspenderSuscripcionUseCase, RenovarSuscripcionUseCase {

    private final ComercioRepository comercioRepository;
    private final ComercioEventPublisher eventPublisher;

    @Override
    public Comercio suspender(String comercioId) {
        Comercio comercio = getComercio(comercioId);
        if (comercio.getEstadoSuscripcion() == EstadoSuscripcion.SUSPENDIDA) {
            return comercio;
        }
        comercio.setEstadoSuscripcion(EstadoSuscripcion.SUSPENDIDA);
        Comercio saved = comercioRepository.save(comercio);
        publish(ComercioEventType.SUSPENDIDO, saved);
        return saved;
    }

    @Override
    public Comercio renovar(String comercioId, PlanSuscripcion plan) {
        Comercio comercio = getComercio(comercioId);
        boolean estabaSuspendida = comercio.getEstadoSuscripcion() == EstadoSuscripcion.SUSPENDIDA;

        // La renovacion cuenta desde el fin vigente si aun no ha vencido
        Instant ahora = Instant.now();
        Instant desde = comercio.getFechaFinSuscripcion() != null
                && comercio.getFechaFinSuscripcion().isAfter(ahora)
                ? comercio.getFechaFinSuscripcion()
                : ahora;

        comercio.setPlan(plan);
        comercio.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
        comercio.setFechaFinSuscripcion(Suscripciones.extender(desde, plan));
        Comercio saved = comercioRepository.save(comercio);

        if (estabaSuspendida) {
            publish(ComercioEventType.REACTIVADO, saved);
        }
        return saved;
    }

    private Comercio getComercio(String id) {
        return comercioRepository.findById(id)
                .orElseThrow(() -> new ComercioNotFoundException(id));
    }

    private void publish(ComercioEventType type, Comercio comercio) {
        eventPublisher.publish(ComercioEvent.builder()
                .type(type)
                .comercio(comercio)
                .timestamp(Instant.now())
                .build());
    }
}
