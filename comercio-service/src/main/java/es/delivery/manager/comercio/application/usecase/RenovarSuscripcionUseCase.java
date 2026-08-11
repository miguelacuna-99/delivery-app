package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.contracts.model.PlanSuscripcion;

/**
 * La plataforma renueva la suscripcion de un comercio: extiende la fecha de fin
 * segun el plan y, si estaba SUSPENDIDA, la reactiva (publica comercio.reactivado).
 */
public interface RenovarSuscripcionUseCase {
    Comercio renovar(String comercioId, PlanSuscripcion plan);
}
