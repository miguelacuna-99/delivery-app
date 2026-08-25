package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.domain.repository.ComercioRepository;
import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComercioServiceTest {

    @Mock
    private ComercioRepository comercioRepository;

    @InjectMocks
    private ComercioService comercioService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("caller-id")
                .username("caller")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Comercio comercioNuevo() {
        return Comercio.builder()
                .nombre("Pizzeria Test")
                .cif("B12345678")
                .direccion("Calle Mayor 1")
                .telefono("600000000")
                .email("info@pizzeria.es")
                .build();
    }

    @Test
    void createComercioArrancaConSuscripcionActivaSegunPlan() {
        when(comercioRepository.existsByCif("B12345678")).thenReturn(false);
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio creado = comercioService.createComercio(comercioNuevo(), PlanSuscripcion.MENSUAL);

        assertThat(creado.isActivo()).isTrue();
        assertThat(creado.getPlan()).isEqualTo(PlanSuscripcion.MENSUAL);
        assertThat(creado.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.ACTIVA);
        assertThat(creado.getFechaInicioSuscripcion()).isNotNull();
        // Un mes vista: entre 28 y 31 dias por delante del inicio
        Duration duracion = Duration.between(creado.getFechaInicioSuscripcion(), creado.getFechaFinSuscripcion());
        assertThat(duracion).isBetween(Duration.ofDays(28), Duration.ofDays(31));
        assertThat(creado.getValorPuntoEuros()).isEqualByComparingTo("0.01");
    }

    @Test
    void createComercioRechazaCifDuplicado() {
        when(comercioRepository.existsByCif("B12345678")).thenReturn(true);

        assertThatThrownBy(() -> comercioService.createComercio(comercioNuevo(), PlanSuscripcion.ANUAL))
                .isInstanceOf(CifAlreadyExistsException.class);

        verify(comercioRepository, never()).save(any());
    }

    @Test
    void getByIdLanzaNotFoundSiNoExiste() {
        when(comercioRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comercioService.getById("nope"))
                .isInstanceOf(ComercioNotFoundException.class);
    }

    @Test
    void listActivosExcluyeComerciosConSuscripcionSuspendida() {
        Comercio operativo = comercioNuevo();
        operativo.setActivo(true);
        operativo.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
        Comercio suspendido = comercioNuevo();
        suspendido.setActivo(true);
        suspendido.setEstadoSuscripcion(EstadoSuscripcion.SUSPENDIDA);
        when(comercioRepository.findAllActivos()).thenReturn(List.of(operativo, suspendido));

        List<Comercio> activos = comercioService.listActivos();

        assertThat(activos).containsExactly(operativo);
    }

    // ROOT y ADMIN pueden actualizar los datos del comercio
    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN"})
    void rootYAdminActualizanSuComercio(TipoUsuario tipo) {
        Comercio existente = comercioNuevo();
        existente.setId("comercio-1");
        existente.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(existente));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio cambios = Comercio.builder()
                .nombre("Nuevo Nombre")
                .direccion("Otra calle 2")
                .telefono("611111111")
                .email("nuevo@pizzeria.es")
                .build();

        Comercio actualizado = comercioService.updateComercio(caller(tipo), cambios);

        assertThat(actualizado.getNombre()).isEqualTo("Nuevo Nombre");
        // El CIF y la suscripcion no se tocan desde este caso de uso
        assertThat(actualizado.getCif()).isEqualTo("B12345678");
        assertThat(actualizado.getEstadoSuscripcion()).isEqualTo(EstadoSuscripcion.ACTIVA);
    }

    @Test
    void actualizarElValorDelPuntoLoCambia() {
        Comercio existente = comercioNuevo();
        existente.setId("comercio-1");
        existente.setValorPuntoEuros(new BigDecimal("0.01"));
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(existente));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio cambios = Comercio.builder().valorPuntoEuros(new BigDecimal("0.05")).build();

        Comercio actualizado = comercioService.updateComercio(caller(TipoUsuario.ADMIN), cambios);

        assertThat(actualizado.getValorPuntoEuros()).isEqualByComparingTo("0.05");
    }

    @Test
    void noMandarValorDelPuntoConservaElActual() {
        Comercio existente = comercioNuevo();
        existente.setId("comercio-1");
        existente.setValorPuntoEuros(new BigDecimal("0.01"));
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(existente));
        when(comercioRepository.save(any(Comercio.class))).thenAnswer(inv -> inv.getArgument(0));

        Comercio actualizado = comercioService.updateComercio(caller(TipoUsuario.ADMIN), comercioNuevo());

        assertThat(actualizado.getValorPuntoEuros()).isEqualByComparingTo("0.01");
    }

    @Test
    void unValorDePuntoNoPositivoSeRechaza() {
        Comercio existente = comercioNuevo();
        existente.setId("comercio-1");
        existente.setValorPuntoEuros(new BigDecimal("0.01"));
        when(comercioRepository.findById("comercio-1")).thenReturn(Optional.of(existente));

        Comercio cambios = Comercio.builder().valorPuntoEuros(BigDecimal.ZERO).build();

        assertThatThrownBy(() -> comercioService.updateComercio(caller(TipoUsuario.ROOT), cambios))
                .isInstanceOf(ValorPuntoInvalidoException.class);
        verify(comercioRepository, never()).save(any());
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"PERSONAL", "REPARTIDOR", "CLIENTE"})
    void otrosTiposNoPuedenActualizarElComercio(TipoUsuario tipo) {
        assertThatThrownBy(() -> comercioService.updateComercio(caller(tipo), comercioNuevo()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(comercioRepository, never()).save(any());
    }
}
