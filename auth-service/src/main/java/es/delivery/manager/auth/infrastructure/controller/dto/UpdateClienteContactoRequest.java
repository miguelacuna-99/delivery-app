package es.delivery.manager.auth.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Campos opcionales: solo se actualizan los que llegan informados.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClienteContactoRequest {
    private String mail;
    private String direccionDomicilio;
    private String telefono;
}
