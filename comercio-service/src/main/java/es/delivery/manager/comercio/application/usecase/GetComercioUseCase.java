package es.delivery.manager.comercio.application.usecase;

import es.delivery.manager.comercio.domain.model.Comercio;

public interface GetComercioUseCase {
    Comercio getById(String id);
}
