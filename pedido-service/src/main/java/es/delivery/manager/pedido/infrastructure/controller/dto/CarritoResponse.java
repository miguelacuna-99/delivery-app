package es.delivery.manager.pedido.infrastructure.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CarritoResponse {
    private String id;
    private String clienteId;
    private String comercioId;
    private List<ItemCarritoDto> items;
    private String codigoCupon;
    private int puntosAplicados;
}
