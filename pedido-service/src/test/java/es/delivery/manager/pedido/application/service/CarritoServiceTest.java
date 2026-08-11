package es.delivery.manager.pedido.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pedido.domain.model.Carrito;
import es.delivery.manager.pedido.domain.model.TokenClaims;
import es.delivery.manager.pedido.domain.repository.CarritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private TokenClaims cliente(String comercioId) {
        return TokenClaims.builder()
                .userId("cliente-1")
                .username("ana")
                .comercioId(comercioId)
                .tipo(TipoUsuario.CLIENTE)
                .build();
    }

    // El cliente esta atado a un unico comercio desde su registro: el comercioId
    // del carrito tiene que salir del token, nunca del que mande el cliente en el body
    @Test
    void updateCarritoIgnoraElComercioIdDelBodyYUsaElDelToken() {
        when(carritoRepository.findByClienteId("cliente-1")).thenReturn(Optional.empty());
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito carritoConComercioAjeno = Carrito.builder()
                .comercioId("comercio-ajeno")
                .items(List.of())
                .build();

        Carrito actualizado = carritoService.updateCarrito(cliente("comercio-1"), carritoConComercioAjeno);

        assertThat(actualizado.getComercioId()).isEqualTo("comercio-1");
        assertThat(actualizado.getClienteId()).isEqualTo("cliente-1");
    }
}
