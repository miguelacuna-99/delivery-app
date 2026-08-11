package es.delivery.manager.pedido.infrastructure.client;

import es.delivery.manager.pedido.domain.model.ProductoCatalogo;
import es.delivery.manager.pedido.domain.service.ProductoPort;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adaptador HTTP de ProductoPort contra el catalogo publico de comercio-service.
 */
@Component
public class ComercioClient implements ProductoPort {

    private final RestClient restClient;

    public ComercioClient(@Value("${comercio.service.url}") String comercioServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(comercioServiceUrl).build();
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
}
