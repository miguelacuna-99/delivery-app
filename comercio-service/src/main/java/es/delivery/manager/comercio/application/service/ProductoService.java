package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.application.usecase.CreateProductoUseCase;
import es.delivery.manager.comercio.application.usecase.DeleteProductoUseCase;
import es.delivery.manager.comercio.application.usecase.ListProductosUseCase;
import es.delivery.manager.comercio.application.usecase.UpdateDisponibilidadProductoUseCase;
import es.delivery.manager.comercio.application.usecase.UpdateProductoUseCase;
import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.domain.repository.ProductoRepository;
import es.delivery.manager.contracts.model.TipoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService implements CreateProductoUseCase, UpdateProductoUseCase,
        UpdateDisponibilidadProductoUseCase, DeleteProductoUseCase, ListProductosUseCase {

    private final ProductoRepository productoRepository;

    @Override
    public Producto createProducto(TokenClaims caller, Producto producto) {
        checkRootOAdmin(caller);
        // Multi-tenant: el producto pertenece siempre al comercio del token
        producto.setComercioId(caller.getComercioId());
        return productoRepository.save(producto);
    }

    @Override
    public Producto updateProducto(TokenClaims caller, String productoId, Producto cambios) {
        checkRootOAdmin(caller);
        Producto producto = getProductoDelComercio(caller, productoId);
        producto.setNombre(cambios.getNombre());
        producto.setDescripcion(cambios.getDescripcion());
        producto.setIngredientes(cambios.getIngredientes());
        producto.setImagenUrl(cambios.getImagenUrl());
        producto.setPrecio(cambios.getPrecio());
        producto.setDisponible(cambios.isDisponible());
        return productoRepository.save(producto);
    }

    @Override
    public Producto updateDisponibilidad(TokenClaims caller, String productoId, boolean disponible) {
        checkRootOAdmin(caller);
        Producto producto = getProductoDelComercio(caller, productoId);
        producto.setDisponible(disponible);
        return productoRepository.save(producto);
    }

    @Override
    public void deleteProducto(TokenClaims caller, String productoId) {
        checkRootOAdmin(caller);
        getProductoDelComercio(caller, productoId);
        productoRepository.deleteById(productoId);
    }

    @Override
    public List<Producto> listByComercio(String comercioId) {
        return productoRepository.findByComercioId(comercioId);
    }

    private Producto getProductoDelComercio(TokenClaims caller, String productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException(productoId));
        // Multi-tenant: nunca tocar productos de otro comercio
        if (!producto.getComercioId().equals(caller.getComercioId())) {
            throw new ProductoNotFoundException(productoId);
        }
        return producto;
    }

    private void checkRootOAdmin(TokenClaims caller) {
        if (caller.getTipo() != TipoUsuario.ROOT && caller.getTipo() != TipoUsuario.ADMIN) {
            throw new ForbiddenOperationException(
                    "El tipo " + caller.getTipo() + " no puede gestionar el catalogo");
        }
    }
}
