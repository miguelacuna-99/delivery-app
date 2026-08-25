package es.delivery.manager.pedido.infrastructure.client;

import es.delivery.manager.pedido.domain.service.FidelidadPort;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adaptador HTTP del puerto FidelidadPort contra fidelidad-service.
 *
 * Sus endpoints internos exigen la clave de servicio a servicio (X-Service-Key):
 * no llevan JWT porque no los invoca un usuario, sino este servicio.
 */
@Slf4j
@Component
public class FidelidadClient implements FidelidadPort {

    private static final String SERVICE_KEY_HEADER = "X-Service-Key";

    private final RestClient restClient;
    private final String serviceApiKey;

    public FidelidadClient(@Value("${fidelidad.service.url}") String fidelidadServiceUrl,
                           @Value("${service.api-key}") String serviceApiKey) {
        this.restClient = RestClient.builder().baseUrl(fidelidadServiceUrl).build();
        this.serviceApiKey = serviceApiKey;
    }

    @Override
    public Optional<BigDecimal> porcentajeCupon(String codigo, String comercioId, String clienteId) {
        CuponValidacionResponse response = restClient.get()
                .uri("/api/cupones/validar?codigo={codigo}&comercioId={comercioId}&clienteId={clienteId}",
                        codigo, comercioId, clienteId)
                .header(SERVICE_KEY_HEADER, serviceApiKey)
                .retrieve()
                .body(CuponValidacionResponse.class);
        if (response == null || !response.isUsable()) {
            return Optional.empty();
        }
        return Optional.of(response.getPorcentajeDescuento());
    }

    @Override
    public int saldoPuntos(String clienteId, String comercioId) {
        SaldoResponse response = restClient.get()
                .uri("/api/puntos/{clienteId}/saldo?comercioId={comercioId}", clienteId, comercioId)
                .header(SERVICE_KEY_HEADER, serviceApiKey)
                .retrieve()
                .body(SaldoResponse.class);
        return response == null ? 0 : response.getSaldo();
    }

    @Data
    static class CuponValidacionResponse {
        private boolean usable;
        private BigDecimal porcentajeDescuento;
    }

    @Data
    static class SaldoResponse {
        private String clienteId;
        private int saldo;
    }
}
