package es.delivery.manager.pedido.infrastructure.mapper;

import es.delivery.manager.contracts.event.ItemPedidoPayload;
import es.delivery.manager.contracts.event.PedidoEventMessage;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.EstadoComercio;
import es.delivery.manager.pedido.domain.model.ItemCarrito;
import es.delivery.manager.pedido.domain.model.Pedido;
import es.delivery.manager.pedido.infrastructure.controller.dto.CarritoResponse;
import es.delivery.manager.pedido.infrastructure.controller.dto.PedidoResponse;
import es.delivery.manager.pedido.infrastructure.controller.dto.UpdateCarritoRequest;
import es.delivery.manager.pedido.infrastructure.repository.CarritoDocument;
import es.delivery.manager.pedido.infrastructure.repository.EstadoComercioDocument;
import es.delivery.manager.pedido.infrastructure.repository.ItemCarritoDocument;
import es.delivery.manager.pedido.infrastructure.repository.PedidoDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    PedidoDocument toDocument(Pedido pedido);

    Pedido toDomain(PedidoDocument document);

    CarritoDocument toDocument(Carrito carrito);

    Carrito toDomain(CarritoDocument document);

    ItemCarritoDocument toDocument(ItemCarrito item);

    ItemCarrito toDomain(ItemCarritoDocument document);

    EstadoComercioDocument toDocument(EstadoComercio estado);

    EstadoComercio toDomain(EstadoComercioDocument document);

    // clienteId y id los fija el service a partir del token
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clienteId", ignore = true)
    Carrito toDomain(UpdateCarritoRequest request);

    CarritoResponse toCarritoResponse(Carrito carrito);

    PedidoResponse toResponse(Pedido pedido);

    ItemPedidoPayload toPayload(ItemCarrito item);

    List<ItemPedidoPayload> toPayload(List<ItemCarrito> items);

    @Mapping(target = "routingKey", ignore = true)
    @Mapping(target = "pedidoId", source = "id")
    @Mapping(target = "timestamp", ignore = true)
    PedidoEventMessage toEventMessage(Pedido pedido);
}
