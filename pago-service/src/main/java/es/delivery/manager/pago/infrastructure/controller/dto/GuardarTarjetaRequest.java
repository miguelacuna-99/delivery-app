package es.delivery.manager.pago.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Alta de tarjeta. El CVV nunca viaja hasta aqui: no hace falta, no hay
 * cobro real, es solo parte del formulario para que se vea realista.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardarTarjetaRequest {
    private String numero;
    private String titular;
    private int mesExpiracion;
    private int anioExpiracion;
}
