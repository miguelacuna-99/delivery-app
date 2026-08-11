package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.Cliente;

public interface RegisterClienteUseCase {
    Cliente register(Cliente cliente);
}
