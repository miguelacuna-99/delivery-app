package es.delivery.manager.fidelidad.domain.model;

public enum TipoMovimiento {
    GANADO,
    CANJEADO,
    DEVUELTO,
    // Puntos GANADOS que se retiran al devolverse el pedido (devolucion.completada)
    RETIRADO
}
