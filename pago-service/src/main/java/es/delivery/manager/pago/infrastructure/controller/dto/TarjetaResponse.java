package es.delivery.manager.pago.infrastructure.controller.dto;

import es.delivery.manager.pago.domain.model.MarcaTarjeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TarjetaResponse {
    private String id;
    private String titular;
    private MarcaTarjeta marca;
    private String ultimos4;
    private int mesExpiracion;
    private int anioExpiracion;
}
