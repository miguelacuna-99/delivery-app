package es.delivery.manager.comercio.application.service;

import es.delivery.manager.comercio.domain.model.Producto;
import es.delivery.manager.comercio.domain.model.TokenClaims;
import es.delivery.manager.comercio.domain.repository.ProductoRepository;
import es.delivery.manager.contracts.model.TipoUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private TokenClaims caller(TipoUsuario tipo) {
        return TokenClaims.builder()
                .userId("caller-id")
                .username("caller")
                .comercioId("comercio-1")
                .tipo(tipo)
                .build();
    }

    private Producto producto() {
        return Producto.builder()
                .nombre("Pizza Margarita")
                .descripcion("Tomate y mozzarella")
                .ingredientes("Tomate, mozzarella, albahaca, aceite de oliva")
                .imagenUrl("https://cdn.example.com/productos/pizza-margarita.jpg")
                .precio(new BigDecimal("9.50"))
                .disponible(true)
                .build();
    }

    // ROOT y ADMIN gestionan el catalogo; el comercioId sale siempre del token
    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"ROOT", "ADMIN"})
    void rootYAdminCreanProductosEnSuComercio(TipoUsuario tipo) {
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto creado = productoService.createProducto(caller(tipo), producto());

        assertThat(creado.getComercioId()).isEqualTo("comercio-1");
    }

    @ParameterizedTest
    @EnumSource(value = TipoUsuario.class, names = {"PERSONAL", "REPARTIDOR", "CLIENTE"})
    void otrosTiposNoPuedenGestionarElCatalogo(TipoUsuario tipo) {
        assertThatThrownBy(() -> productoService.createProducto(caller(tipo), producto()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void updateProductoModificaSoloProductosDelPropioComercio() {
        Producto existente = producto();
        existente.setId("prod-1");
        existente.setComercioId("comercio-1");
        when(productoRepository.findById("prod-1")).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto cambios = producto();
        cambios.setPrecio(new BigDecimal("11.00"));
        cambios.setDisponible(false);
        cambios.setIngredientes("Tomate, mozzarella, albahaca");
        cambios.setImagenUrl("https://cdn.example.com/productos/pizza-margarita-v2.jpg");

        Producto actualizado = productoService.updateProducto(caller(TipoUsuario.ADMIN), "prod-1", cambios);

        assertThat(actualizado.getPrecio()).isEqualByComparingTo("11.00");
        assertThat(actualizado.isDisponible()).isFalse();
        assertThat(actualizado.getComercioId()).isEqualTo("comercio-1");
        assertThat(actualizado.getIngredientes()).isEqualTo("Tomate, mozzarella, albahaca");
        assertThat(actualizado.getImagenUrl()).isEqualTo("https://cdn.example.com/productos/pizza-margarita-v2.jpg");
    }

    @Test
    void updateDisponibilidadCambiaSoloElFlagYConservaElResto() {
        Producto existente = producto();
        existente.setId("prod-1");
        existente.setComercioId("comercio-1");
        when(productoRepository.findById("prod-1")).thenReturn(Optional.of(existente));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.updateDisponibilidad(caller(TipoUsuario.ADMIN), "prod-1", false);

        assertThat(actualizado.isDisponible()).isFalse();
        assertThat(actualizado.getNombre()).isEqualTo("Pizza Margarita");
        assertThat(actualizado.getDescripcion()).isEqualTo("Tomate y mozzarella");
        assertThat(actualizado.getIngredientes()).isEqualTo("Tomate, mozzarella, albahaca, aceite de oliva");
        assertThat(actualizado.getImagenUrl()).isEqualTo("https://cdn.example.com/productos/pizza-margarita.jpg");
        assertThat(actualizado.getPrecio()).isEqualByComparingTo("9.50");
    }

    @Test
    void updateDisponibilidadDeOtroComercioSeTrataComoNotFound() {
        Producto ajeno = producto();
        ajeno.setId("prod-2");
        ajeno.setComercioId("comercio-2");
        when(productoRepository.findById("prod-2")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> productoService.updateDisponibilidad(caller(TipoUsuario.ROOT), "prod-2", false))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void updateProductoDeOtroComercioSeTrataComoNotFound() {
        Producto ajeno = producto();
        ajeno.setId("prod-2");
        ajeno.setComercioId("comercio-2");
        when(productoRepository.findById("prod-2")).thenReturn(Optional.of(ajeno));

        assertThatThrownBy(() -> productoService.updateProducto(caller(TipoUsuario.ROOT), "prod-2", producto()))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(productoRepository, never()).save(any());
    }

    @Test
    void deleteProductoEliminaTrasComprobarPertenencia() {
        Producto existente = producto();
        existente.setId("prod-1");
        existente.setComercioId("comercio-1");
        when(productoRepository.findById("prod-1")).thenReturn(Optional.of(existente));

        productoService.deleteProducto(caller(TipoUsuario.ROOT), "prod-1");

        verify(productoRepository).deleteById("prod-1");
    }

    @Test
    void deleteProductoInexistenteLanzaNotFound() {
        when(productoRepository.findById("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productoService.deleteProducto(caller(TipoUsuario.ROOT), "nope"))
                .isInstanceOf(ProductoNotFoundException.class);

        verify(productoRepository, never()).deleteById(any());
    }
}
