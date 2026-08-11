package es.delivery.manager.pedido.application.service;

import es.delivery.manager.pedido.application.usecase.ActualizarEstadoComercioUseCase;
import es.delivery.manager.pedido.domain.model.EstadoComercio;
import es.delivery.manager.pedido.domain.repository.EstadoComercioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstadoComercioService implements ActualizarEstadoComercioUseCase {

    private final EstadoComercioRepository estadoComercioRepository;

    @Override
    public void marcarSuspendido(String comercioId) {
        guardar(comercioId, false);
        log.info("Comercio {} suspendido: deja de admitir pedidos", comercioId);
    }

    @Override
    public void marcarOperativo(String comercioId) {
        guardar(comercioId, true);
        log.info("Comercio {} reactivado: vuelve a admitir pedidos", comercioId);
    }

    private void guardar(String comercioId, boolean operativo) {
        EstadoComercio estado = estadoComercioRepository.findByComercioId(comercioId)
                .orElseGet(() -> EstadoComercio.builder().comercioId(comercioId).build());
        estado.setOperativo(operativo);
        estado.setFechaActualizacion(Instant.now());
        estadoComercioRepository.save(estado);
    }
}
