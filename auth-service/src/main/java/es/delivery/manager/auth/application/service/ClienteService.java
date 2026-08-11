package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.application.usecase.RegisterClienteUseCase;
import es.delivery.manager.auth.application.usecase.UpdateClienteContactoUseCase;
import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.PasswordPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService implements RegisterClienteUseCase, UpdateClienteContactoUseCase {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordPort passwordPort;

    @Override
    public Cliente register(Cliente cliente) {
        // Cada web es de un unico comercio: el cliente queda atado a el desde el registro
        if (cliente.getComercioId() == null || cliente.getComercioId().isBlank()) {
            throw new ComercioIdRequeridoException();
        }

        // El username debe ser unico en toda la plataforma (clientes y usuarios de comercio)
        if (clienteRepository.existsByUsername(cliente.getUsername())
                || usuarioRepository.findByUsername(cliente.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException(cliente.getUsername());
        }

        // passwordHash contiene el password raw que viene del mapper
        cliente.setPasswordHash(passwordPort.encode(cliente.getPasswordHash()));
        cliente.setTipo(TipoUsuario.CLIENTE);
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente updateContacto(String clienteId, String mail, String direccionDomicilio, String telefono) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNotFoundException(clienteId));

        if (mail != null) {
            cliente.setMail(mail);
        }
        if (direccionDomicilio != null) {
            cliente.setDireccionDomicilio(direccionDomicilio);
        }
        if (telefono != null) {
            cliente.setTelefono(telefono);
        }
        return clienteRepository.save(cliente);
    }
}
