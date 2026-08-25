package es.delivery.manager.pedido.infrastructure.client;

import es.delivery.manager.pedido.domain.model.ProductoCatalogo;
import es.delivery.manager.pedido.domain.service.ComercioPort;
import es.delivery.manager.pedido.domain.service.ProductoPort;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adaptador HTTP contra comercio-service: catalogo (ProductoPort) y datos de
 * configuracion del comercio como el valor del punto (ComercioPort).
 */
@Component
public class ComercioClient implements ProductoPort, ComercioPort {

    private static final BigDecimal VALOR_PUNTO_POR_DEFECTO = new BigDecimal("0.01");

    private final RestClient restClient;

    public ComercioClient(@Value("${comercio.service.url}") String comercioServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(comercioServiceUrl).build();
    }

    @Override
    public BigDecimal getValorPunto(String comercioId) {
        ComercioResponse respuesta = restClient.get()
                .uri("/api/comercios/{comercioId}", comercioId)
                .retrieve()
                .body(ComercioResponse.class);
        if (respuesta == null || respuesta.getValorPuntoEuros() == null) {
            return VALOR_PUNTO_POR_DEFECTO;
        }
        return respuesta.getValorPuntoEuros();
    }

    @Override
    public List<ProductoCatalogo> catalogoDe(String comercioId) {
        List<ProductoResponse> respuesta = restClient.get()
                .uri("/api/comercios/{comercioId}/productos", comercioId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<ProductoResponse>>() {
                });
        if (respuesta == null) {
            return List.of();
        }
        return respuesta.stream()
                .map(p -> ProductoCatalogo.builder()
                        .id(p.getId())
                        .nombre(p.getNombre())
                        .ingredientes(p.getIngredientes())
                        .imagenUrl(p.getImagenUrl())
                        .precio(p.getPrecio())
                        .disponible(p.isDisponible())
                        .build())
                .toList();
    }

    @Data
    static class ProductoResponse {
        private String id;
        private String comercioId;
        private String nombre;
        private String descripcion;
        private String ingredientes;
        private String imagenUrl;
        private BigDecimal precio;
        private boolean disponible;
    }

    @Data
    static class ComercioResponse {
        private String id;
        private BigDecimal valorPuntoEuros;
    }
}
