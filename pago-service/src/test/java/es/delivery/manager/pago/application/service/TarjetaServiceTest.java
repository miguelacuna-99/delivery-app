package es.delivery.manager.pago.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pago.domain.model.MarcaTarjeta;
import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.domain.repository.TarjetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarjetaServiceTest {

    @Mock
    private TarjetaRepository tarjetaRepository;

    @InjectMocks
    private TarjetaService tarjetaService;

    private TokenClaims cliente() {
        return TokenClaims.builder().userId("cliente-1").username("ana").tipo(TipoUsuario.CLIENTE).build();
    }

    private YearMonth futuro() {
        return YearMonth.now().plusYears(2);
    }

    // 4242424242424242 es el numero de prueba estandar de Visa (pasa Luhn)
    @Test
    void guardarTarjetaValidaDetectaMarcaYUltimos4() {
        when(tarjetaRepository.save(any(Tarjeta.class))).thenAnswer(inv -> inv.getArgument(0));
        YearMonth exp = futuro();

        Tarjeta tarjeta = tarjetaService.guardarTarjeta(cliente(), "4242 4242 4242 4242", "Ana Garcia",
                exp.getMonthValue(), exp.getYear());

        assertThat(tarjeta.getClienteId()).isEqualTo("cliente-1");
        assertThat(tarjeta.getMarca()).isEqualTo(MarcaTarjeta.VISA);
        assertThat(tarjeta.getUltimos4()).isEqualTo("4242");
        assertThat(tarjeta.getTitular()).isEqualTo("Ana Garcia");
    }

    @Test
    void guardarTarjetaConLuhnInvalidoLanza400() {
        YearMonth exp = futuro();

        assertThatThrownBy(() -> tarjetaService.guardarTarjeta(cliente(), "1234567812345678", "Ana Garcia",
                exp.getMonthValue(), exp.getYear()))
                .isInstanceOf(TarjetaInvalidaException.class);

        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void guardarTarjetaCaducadaLanza400() {
        YearMonth pasado = YearMonth.now().minusMonths(1);

        assertThatThrownBy(() -> tarjetaService.guardarTarjeta(cliente(), "4242424242424242", "Ana Garcia",
                pasado.getMonthValue(), pasado.getYear()))
                .isInstanceOf(TarjetaInvalidaException.class);

        verify(tarjetaRepository, never()).save(any());
    }

    @Test
    void guardarTarjetaSinTitularLanza400() {
        YearMonth exp = futuro();

        assertThatThrownBy(() -> tarjetaService.guardarTarjeta(cliente(), "4242424242424242", "  ",
                exp.getMonthValue(), exp.getYear()))
                .isInstanceOf(TarjetaInvalidaException.class);
    }

    @Test
    void unUsuarioQueNoEsClienteNoPuedeGuardarTarjetas() {
        TokenClaims admin = TokenClaims.builder().userId("u1").comercioId("comercio-1").tipo(TipoUsuario.ADMIN).build();
        YearMonth exp = futuro();

        assertThatThrownBy(() -> tarjetaService.guardarTarjeta(admin, "4242424242424242", "Ana",
                exp.getMonthValue(), exp.getYear()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void listMisTarjetasDevuelveSoloLasDelCliente() {
        Tarjeta t1 = Tarjeta.builder().id("t1").clienteId("cliente-1").fechaAlta(Instant.now()).build();
        when(tarjetaRepository.findByClienteId("cliente-1")).thenReturn(List.of(t1));

        List<Tarjeta> tarjetas = tarjetaService.listMisTarjetas(cliente());

        assertThat(tarjetas).containsExactly(t1);
    }

    @Test
    void eliminarTarjetaPropiaLaBorra() {
        Tarjeta tarjeta = Tarjeta.builder().id("t1").clienteId("cliente-1").build();
        when(tarjetaRepository.findById("t1")).thenReturn(Optional.of(tarjeta));

        tarjetaService.eliminarTarjeta(cliente(), "t1");

        verify(tarjetaRepository).deleteById("t1");
    }

    @Test
    void eliminarTarjetaDeOtroClienteSeTrataComoNotFound() {
        Tarjeta ajena = Tarjeta.builder().id("t2").clienteId("otro-cliente").build();
        when(tarjetaRepository.findById("t2")).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> tarjetaService.eliminarTarjeta(cliente(), "t2"))
                .isInstanceOf(TarjetaNotFoundException.class);

        verify(tarjetaRepository, never()).deleteById(any());
    }

    @Test
    void eliminarTarjetaInexistenteLanzaNotFound() {
        when(tarjetaRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tarjetaService.eliminarTarjeta(cliente(), "nope"))
                .isInstanceOf(TarjetaNotFoundException.class);
    }
}
