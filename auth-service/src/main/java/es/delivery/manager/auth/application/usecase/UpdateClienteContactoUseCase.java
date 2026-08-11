package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.domain.model.Cliente;

public interface UpdateClienteContactoUseCase {
    Cliente updateContacto(String clienteId, String mail, String direccionDomicilio, String telefono);
}
