package es.delivery.manager.contracts.model;

/**
 * ACTIVA: al dia. EN_GRACIA: vencida pero dentro del periodo de gracia (sigue operando).
 * SUSPENDIDA: sin acceso hasta que la plataforma renueve.
 */
public enum EstadoSuscripcion {
    ACTIVA,
    EN_GRACIA,
    SUSPENDIDA
}
