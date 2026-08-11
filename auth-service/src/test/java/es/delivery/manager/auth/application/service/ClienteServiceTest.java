package es.delivery.manager.auth.application.service;

import es.delivery.manager.auth.domain.model.Cliente;
import es.delivery.manager.auth.domain.repository.ClienteRepository;
import es.delivery.manager.auth.domain.repository.UsuarioRepository;
import es.delivery.manager.auth.domain.service.PasswordPort;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordPort passwordPort;

    @InjectMocks
    private ClienteService clienteService;

    private Cliente cliente(String comercioId) {
        return Cliente.builder()
                .comercioId(comercioId)
                .username("ana")
                .passwordHash("secret")
                .mail("ana@example.com")
                .build();
    }

    @Test
    void registerSinComercioIdLanzaExcepcion() {
        assertThatThrownBy(() -> clienteService.register(cliente(null)))
                .isInstanceOf(ComercioIdRequeridoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void registerConComercioIdEnBlancoLanzaExcepcion() {
        assertThatThrownBy(() -> clienteService.register(cliente("  ")))
                .isInstanceOf(ComercioIdRequeridoException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void registerConComercioIdCreaClienteAtadoAEseComercio() {
        when(clienteRepository.existsByUsername("ana")).thenReturn(false);
        when(usuarioRepository.findByUsername("ana")).thenReturn(java.util.Optional.empty());
        when(passwordPort.encode("secret")).thenReturn("hash");
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente registrado = clienteService.register(cliente("comercio-1"));

        assertThat(registrado.getComercioId()).isEqualTo("comercio-1");
        assertThat(registrado.getTipo()).isEqualTo(TipoUsuario.CLIENTE);
        assertThat(registrado.getPasswordHash()).isEqualTo("hash");
    }
}
