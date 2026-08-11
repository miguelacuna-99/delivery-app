package es.delivery.manager.comercio.application.service;

import es.delivery.manager.contracts.model.PlanSuscripcion;

import java.time.Instant;
import java.time.ZoneOffset;

/**
 * Calculo de fechas de suscripcion segun el plan contratado.
 */
final class Suscripciones {

    static Instant extender(Instant desde, PlanSuscripcion plan) {
        var base = desde.atZone(ZoneOffset.UTC);
        return switch (plan) {
            case MENSUAL -> base.plusMonths(1).toInstant();
            case ANUAL -> base.plusYears(1).toInstant();
        };
    }

    private Suscripciones() {
    }
}
