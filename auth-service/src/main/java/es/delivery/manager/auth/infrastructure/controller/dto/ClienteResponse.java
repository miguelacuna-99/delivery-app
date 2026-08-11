package es.delivery.manager.auth.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private String id;
    private String comercioId;
    private String username;
    private String mail;
    private String direccionDomicilio;
    private String telefono;
}
