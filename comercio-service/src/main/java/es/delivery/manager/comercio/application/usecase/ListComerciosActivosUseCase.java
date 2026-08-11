package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;

import java.util.List;

/**
 * Listado publico de comercios operativos para la app de clientes.
 */
public interface ListComerciosActivosUseCase {
    List<Comercio> listActivos();
}
