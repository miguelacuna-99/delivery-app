package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.application.usecase.CreateComercioUseCase;
import es.delivery.manager.comercio.application.usecase.GetComercioUseCase;
import es.delivery.manager.comercio.application.usecase.ListComerciosActivosUseCase;
import es.delivery.manager.comercio.application.usecase.UpdateComercioUseCase;
import es.delivery.manager.comercio.domain.model.Comercio;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.domain.repository.ComercioRepository;
import es.delivery.manager.contracts.model.EstadoSuscripcion;
import es.delivery.manager.contracts.model.PlanSuscripcion;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComercioService implements CreateComercioUseCase, GetComercioUseCase,
        ListComerciosActivosUseCase, UpdateComercioUseCase {

    private final ComercioRepository comercioRepository;

    // Mismo valor que la antigua constante global VALOR_PUNTO de pedido-service:
    // nada cambia hasta que un ROOT/ADMIN lo personalice desde "Mi comercio"
    private static final BigDecimal VALOR_PUNTO_POR_DEFECTO = new BigDecimal("0.01");

    @Override
    public Comercio createComercio(Comercio comercio, PlanSuscripcion plan) {
        if (comercioRepository.existsByCif(comercio.getCif())) {
            throw new CifAlreadyExistsException(comercio.getCif());
        }
        Instant ahora = Instant.now();
        comercio.setActivo(true);
        comercio.setPlan(plan);
        comercio.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);
        comercio.setFechaInicioSuscripcion(ahora);
        comercio.setFechaFinSuscripcion(Suscripciones.extender(ahora, plan));
        comercio.setValorPuntoEuros(VALOR_PUNTO_POR_DEFECTO);
        return comercioRepository.save(comercio);
    }

    @Override
    public Comercio getById(String id) {
        return comercioRepository.findById(id)
                .orElseThrow(() -> new ComercioNotFoundException(id));
    }

    @Override
    public List<Comercio> listActivos() {
        return comercioRepository.findAllActivos().stream()
                .filter(Comercio::esOperativo)
                .toList();
    }

    @Override
    public Comercio updateComercio(TokenClaims caller, Comercio cambios) {
        checkRootOAdmin(caller);
        // Multi-tenant: el comercio a modificar sale siempre del token, nunca del body
        Comercio comercio = getById(caller.getComercioId());
        comercio.setNombre(cambios.getNombre());
        comercio.setDireccion(cambios.getDireccion());
        comercio.setTelefono(cambios.getTelefono());
        comercio.setEmail(cambios.getEmail());
        if (cambios.getValorPuntoEuros() != null) {
            if (cambios.getValorPuntoEuros().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValorPuntoInvalidoException();
            }
            comercio.setValorPuntoEuros(cambios.getValorPuntoEuros());
        }
        return comercioRepository.save(comercio);
    }

    private void checkRootOAdmin(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.ROOT && caller.getTipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede modificar los datos del comercio");
        }
    }
}
