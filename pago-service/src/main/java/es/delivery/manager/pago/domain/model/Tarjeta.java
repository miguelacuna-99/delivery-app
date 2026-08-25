package es.delivery.manager.pago.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Tarjeta guardada de un cliente. Nunca se persiste el numero completo ni el
 * CVV -- solo lo necesario para mostrarla (marca, ultimos 4 digitos, caducidad).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tarjeta {
    private String id;
    private String clienteId;
    private String titular;
    private MarcaTarjeta marca;
    private String ultimos4;
    private int mesExpiracion;
    private int anioExpiracion;
    private Instant fechaAlta;
}
