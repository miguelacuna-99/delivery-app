package es.delivery.manager.fidelidad.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.EstadoCupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.domain.model.UsoCupon;
import es.delivery.manager.fidelidad.domain.repository.CuponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuponServiceTest {

    @Mock
    private CuponRepository cuponRepository;

    @InjectMocks
    private CuponService cuponService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("user-1")
                .username("user")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Cupon cupon(EstadoCupon estado, Instant caducidad) {
        return Cupon.builder()
                .id("cupon-1")
                .comercioId("comercio-1")
                .codigo("PROMO10")
                .porcentajeDescuento(new BigDecimal("10"))
                .usosMaximosPorUsuario(2)
                .usos(new ArrayList<>())
                .estado(estado)
                .fechaCaducidad(caducidad)
                .build();
    }

    @Test
    void soloElAdminCreaCupones() {
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.empty());
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Cupon creado = cuponService.createCupon(caller(TipoUsuario.ADMIN),
                cupon(null, null));

        assertThat(creado.getEstado()).isEqualTo(EstadoCupon.ACTIVO);
        assertThat(creado.getComercioId()).isEqualTo("comercio-1");
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "PERSONAL", "REPARTIDOR", "CLIENTE"})
    void nadieMasCreaCupones(TipoUsuario tipo) {
        assertThatThrownBy(() -> cuponService.createCupon(caller(tipo), cupon(null, null)))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(cuponRepository, never()).save(any());
    }

    @Test
    void codigoDuplicadoRechazado() {
        when(cuponRepository.findByCodigo("PROMO10"))
                .thenReturn(Optional.of(cupon(EstadoCupon.ACTIVO, null)));

        assertThatThrownBy(() -> cuponService.createCupon(caller(TipoUsuario.ADMIN), cupon(null, null)))
                .isInstanceOf(CodigoCuponDuplicadoException.class);
    }

    @Test
    void anularCambiaElEstado() {
        when(cuponRepository.findById("cupon-1")).thenReturn(Optional.of(cupon(EstadoCupon.ACTIVO, null)));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Cupon anulado = cuponService.anularCupon(caller(TipoUsuario.ADMIN), "cupon-1");

        assertThat(anulado.getEstado()).isEqualTo(EstadoCupon.ANULADO);
    }

    @Test
    void noSeAnulanCuponesDeOtroComercio() {
        Cupon ajeno = cupon(EstadoCupon.ACTIVO, null);
        ajeno.setComercioId("comercio-2");
        when(cuponRepository.findById("cupon-1")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> cuponService.anularCupon(caller(TipoUsuario.ADMIN), "cupon-1"))
                .isInstanceOf(CuponNotFoundException.class);
    }

    @Test
    void validarDevuelvePorcentajeSiEsUsable() {
        when(cuponRepository.findByCodigo("PROMO10"))
                .thenReturn(Optional.of(cupon(EstadoCupon.ACTIVO, Instant.now().plus(1, ChronoUnit.DAYS))));

        Optional<BigDecimal> porcentaje = cuponService.validarCupon("PROMO10", "comercio-1", "cliente-1");

        assertThat(porcentaje).contains(new BigDecimal("10"));
    }

    @Test
    void validarCaducadoPersisteElEstadoYDevuelveVacio() {
        Cupon vencido = cupon(EstadoCupon.ACTIVO, Instant.now().minus(1, ChronoUnit.DAYS));
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.of(vencido));
        when(cuponRepository.save(any(Cupon.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<BigDecimal> porcentaje = cuponService.validarCupon("PROMO10", "comercio-1", "cliente-1");

        assertThat(porcentaje).isEmpty();
        verify(cuponRepository).save(any(Cupon.class));
        assertThat(vencido.getEstado()).isEqualTo(EstadoCupon.CADUCADO);
    }

    @Test
    void validarConUsosAgotadosDevuelveVacio() {
        Cupon agotado = cupon(EstadoCupon.ACTIVO, null);
        agotado.setUsos(new ArrayList<>(List.of(UsoCupon.builder().clienteId("cliente-1").contador(2).build())));
        when(cuponRepository.findByCodigo("PROMO10")).thenReturn(Optional.of(agotado));

        Optional<BigDecimal> porcentaje = cuponService.validarCupon("PROMO10", "comercio-1", "cliente-1");

        assertThat(porcentaje).isEmpty();
    }

    @Test
    void validarCuponDeOtroComercioDevuelveVacio() {
        when(cuponRepository.findByCodigo("PROMO10"))
                .thenReturn(Optional.of(cupon(EstadoCupon.ACTIVO, null)));

        Optional<BigDecimal> porcentaje = cuponService.validarCupon("PROMO10", "comercio-2", "cliente-1");

        assertThat(porcentaje).isEmpty();
    }
}
