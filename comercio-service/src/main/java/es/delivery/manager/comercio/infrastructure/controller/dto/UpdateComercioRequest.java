package es.delivery.manager.comercio.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComercioRequest {
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
}
