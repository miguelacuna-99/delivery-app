package es.delivery.manager.fidelidad.infrastructure.repository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsoCuponDocument {
    private String clienteId;
    private int contador;
}
