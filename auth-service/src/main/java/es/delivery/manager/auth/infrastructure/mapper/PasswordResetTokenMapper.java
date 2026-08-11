package es.delivery.manager.auth.infrastructure.mapper;

import es.delivery.manager.auth.domain.model.PasswordResetToken;
import es.delivery.manager.auth.infrastructure.repository.PasswordResetTokenDocument;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PasswordResetTokenMapper {

    PasswordResetTokenDocument toDocument(PasswordResetToken token);

    PasswordResetToken toDomain(PasswordResetTokenDocument document);
}
