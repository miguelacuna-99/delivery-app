package es.delivery.manager.pago.infrastructure.mapper;

import es.delivery.manager.contracts.event.PagoEventMessage;
import es.delivery.manager.pago.domain.model.Pago;
import es.delivery.manager.pago.infrastructure.repository.PagoDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PagoMapper {

    PagoDocument toDocument(Pago pago);

    Pago toDomain(PagoDocument document);

    @Mapping(target = "routingKey", ignore = true)
    @Mapping(target = "pagoId", source = "id")
    @Mapping(target = "timestamp", ignore = true)
    PagoEventMessage toEventMessage(Pago pago);
}
