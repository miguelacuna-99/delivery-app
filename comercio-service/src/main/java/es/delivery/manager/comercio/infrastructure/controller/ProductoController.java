package es.delivery.manager.comercio.infrastructure.controller;

import es.delivery.manager.comercio.application.usecase.CreateProductoUseCase;
import es.delivery.manager.comercio.application.usecase.DeleteProductoUseCase;
import es.delivery.manager.comercio.application.usecase.ListProductosUseCase;
import es.delivery.manager.comercio.application.usecase.UpdateProductoUseCase;
import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.infrastructure.controller.dto.ProductoRequest;
import es.delivery.manager.comercio.infrastructure.controller.dto.ProductoResponse;
import es.delivery.manager.comercio.infrastructure.mapper.ProductoMapper;
import es.delivery.manager.comercio.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductoController {

    private final CreateProductoUseCase createProductoUseCase;
    private final UpdateProductoUseCase updateProductoUseCase;
    private final DeleteProductoUseCase deleteProductoUseCase;
    private final ListProductosUseCase listProductosUseCase;
    private final ProductoMapper productoMapper;

    // Catalogo publico de un comercio
    @GetMapping("/api/comercios/{comercioId}/productos")
    public List<ProductoResponse> listByComercio(@PathVariable String comercioId) {
        return listProductosUseCase.listByComercio(comercioId).stream()
                .map(productoMapper::toResponse)
                .toList();
    }

    // ROOT o ADMIN crean productos en el catalogo de su comercio
    @PostMapping("/api/productos")
    public ResponseEntity<ProductoResponse> createProducto(@RequestBody ProductoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Producto producto = createProductoUseCase.createProducto(caller, productoMapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(productoMapper.toResponse(producto));
    }

    @PutMapping("/api/productos/{id}")
    public ProductoResponse updateProducto(@PathVariable String id, @RequestBody ProductoRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Producto producto = updateProductoUseCase.updateProducto(caller, id, productoMapper.toDomain(request));
        return productoMapper.toResponse(producto);
    }

    @DeleteMapping("/api/productos/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable String id) {
        TokenClaims caller = RequestSecurityContext.require();
        deleteProductoUseCase.deleteProducto(caller, id);
        return ResponseEntity.noContent().build();
    }
}
