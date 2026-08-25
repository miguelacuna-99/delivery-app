package es.delivery.manager.pago.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.pago.application.usecase.EliminarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.GuardarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.ListTarjetasUseCase;
import es.delivery.manager.pago.domain.model.MarcaTarjeta;
import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.domain.repository.TarjetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarjetaService implements GuardarTarjetaUseCase, ListTarjetasUseCase, EliminarTarjetaUseCase {

    private final TarjetaRepository tarjetaRepository;

    @Override
    public Tarjeta guardarTarjeta(TokenClaims caller, String numero, String titular,
                                   int mesExpiracion, int anioExpiracion) {
        checkCliente(caller);
        String limpio = numero == null ? "" : numero.replaceAll("\\s+", "");
        if (!esLongitudValida(limpio) || !pasaLuhn(limpio)) {
            throw new TarjetaInvalidaException("Numero de tarjeta invalido");
        }
        if (titular == null || titular.isBlank()) {
            throw new TarjetaInvalidaException("El titular es obligatorio");
        }
        if (estaCaducada(mesExpiracion, anioExpiracion)) {
            throw new TarjetaInvalidaException("La tarjeta esta caducada");
        }

        Tarjeta tarjeta = Tarjeta.builder()
                .clienteId(caller.getUserId())
                .titular(titular)
                .marca(detectarMarca(limpio))
                .ultimos4(limpio.substring(limpio.length() - 4))
                .mesExpiracion(mesExpiracion)
                .anioExpiracion(anioExpiracion)
                .fechaAlta(Instant.now())
                .build();
        return tarjetaRepository.save(tarjeta);
    }

    @Override
    public List<Tarjeta> listMisTarjetas(TokenClaims caller) {
        checkCliente(caller);
        return tarjetaRepository.findByClienteId(caller.getUserId());
    }

    @Override
    public void eliminarTarjeta(TokenClaims caller, String tarjetaId) {
        checkCliente(caller);
        // No confirmar si el id existe: una tarjeta de otro cliente se trata como inexistente
        Tarjeta tarjeta = tarjetaRepository.findById(tarjetaId)
                .filter(t -> t.getClienteId().equals(caller.getUserId()))
                .orElseThrow(() -> new TarjetaNotFoundException(tarjetaId));
        tarjetaRepository.deleteById(tarjeta.getId());
    }

    private void checkCliente(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.CLIENTE) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede gestionar tarjetas");
        }
    }

    private boolean esLongitudValida(String numero) {
        return numero.matches("\\d{13,19}");
    }

    private boolean pasaLuhn(String numero) {
        int suma = 0;
        boolean doblar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (doblar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            doblar = !doblar;
        }
        return suma % 10 == 0;
    }

    private boolean estaCaducada(int mes, int anio) {
        YearMonth caducidad = YearMonth.of(anio, mes);
        return caducidad.isBefore(YearMonth.now());
    }

    private MarcaTarjeta detectarMarca(String numero) {
        if (numero.startsWith("4")) {
            return MarcaTarjeta.VISA;
        }
        int prefijo2 = Integer.parseInt(numero.substring(0, 2));
        int prefijo4 = Integer.parseInt(numero.substring(0, 4));
        if ((prefijo2 >= 51 && prefijo2 <= 55) || (prefijo4 >= 2221 && prefijo4 <= 2720)) {
            return MarcaTarjeta.MASTERCARD;
        }
        return MarcaTarjeta.OTRA;
    }
}
