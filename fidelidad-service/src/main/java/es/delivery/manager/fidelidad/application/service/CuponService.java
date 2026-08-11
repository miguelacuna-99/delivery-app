package es.delivery.manager.fidelidad.application.service;

import es.delivery.manager.contracts.model.TipoUsuario;
import es.delivery.manager.fidelidad.application.usecase.AnularCuponUseCase;
import es.delivery.manager.fidelidad.application.usecase.CreateCuponUseCase;
import es.delivery.manager.fidelidad.application.usecase.ListCuponesUseCase;
import es.delivery.manager.fidelidad.application.usecase.ValidarCuponUseCase;
import es.delivery.manager.fidelidad.domain.model.Cupon;
import es.delivery.manager.fidelidad.domain.model.EstadoCupon;
import es.delivery.manager.fidelidad.domain.model.TokenClaims;
import es.delivery.manager.fidelidad.domain.model.UsoCupon;
import es.delivery.manager.fidelidad.domain.repository.CuponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CuponService implements CreateCuponUseCase, AnularCuponUseCase,
        ListCuponesUseCase, ValidarCuponUseCase {

    private final CuponRepository cuponRepository;

    @Override
    public Cupon createCupon(TokenClaims caller, Cupon cupon) {
        checkAdmin(caller);
        if (cuponRepository.findByCodigo(cupon.getCodigo()).isPresent()) {
            throw new CodigoCuponDuplicadoException(cupon.getCodigo());
        }
        // Multi-tenant: el cupon pertenece siempre al comercio del token
        cupon.setComercioId(caller.getComercioId());
        cupon.setEstado(EstadoCupon.ACTIVO);
        cupon.setUsos(new ArrayList<>());
        return cuponRepository.save(cupon);
    }

    @Override
    public Cupon anularCupon(TokenClaims caller, String cuponId) {
        checkAdmin(caller);
        Cupon cupon = cuponRepository.findById(cuponId)
                .filter(c -> c.getComercioId().equals(caller.getComercioId()))
                .orElseThrow(() -> new CuponNotFoundException(cuponId));
        cupon.setEstado(EstadoCupon.ANULADO);
        return cuponRepository.save(cupon);
    }

    @Override
    public List<Cupon> listCupones(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.ROOT && caller.getTipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede consultar los cupones");
        }
        return cuponRepository.findByComercioId(caller.getComercioId());
    }

    @Override
    public Optional<BigDecimal> validarCupon(String codigo, String comercioId, String clienteId) {
        Instant ahora = Instant.now();
        Optional<Cupon> encontrado = cuponRepository.findByCodigo(codigo)
                .filter(c -> c.getComercioId().equals(comercioId));
        if (encontrado.isEmpty()) {
            return Optional.empty();
        }
        Cupon cupon = encontrado.get();

        // Caducidad perezosa: al validar un ACTIVO vencido se persiste como CADUCADO
        if (cupon.getEstado() == EstadoCupon.ACTIVO && !cupon.esUsable(ahora)) {
            cupon.setEstado(EstadoCupon.CADUCADO);
            cuponRepository.save(cupon);
            return Optional.empty();
        }
        if (!cupon.esUsable(ahora) || usosAgotados(cupon, clienteId)) {
            return Optional.empty();
        }
        return Optional.of(cupon.getPorcentajeDescuento());
    }

    private boolean usosAgotados(Cupon cupon, String clienteId) {
        return usosDe(cupon, clienteId)
                .map(uso -> uso.getContador() >= cupon.getUsosMaximosPorUsuario())
                .orElse(false);
    }

    static Optional<UsoCupon> usosDe(Cupon cupon, String clienteId) {
        if (cupon.getUsos() == null) {
            return Optional.empty();
        }
        return cupon.getUsos().stream()
                .filter(u -> u.getClienteId().equals(clienteId))
                .findFirst();
    }

    private void checkAdmin(TokenClaims caller) {
        // Decision de diseno: solo el ADMIN del comercio crea o anula cupones
        if (caller.getTipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenOperationException(
                    "Solo el ADMIN del comercio gestiona cupones (llamante: " + caller.getTipo() + ")");
        }
    }
}
