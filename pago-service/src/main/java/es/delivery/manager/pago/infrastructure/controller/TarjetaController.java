package es.delivery.manager.pago.infrastructure.controller;

import es.delivery.manager.pago.application.usecase.EliminarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.GuardarTarjetaUseCase;
import es.delivery.manager.pago.application.usecase.ListTarjetasUseCase;
import es.delivery.manager.pago.domain.model.Tarjeta;
import es.delivery.manager.pago.domain.model.TokenClaims;
import es.delivery.manager.pago.infrastructure.controller.dto.GuardarTarjetaRequest;
import es.delivery.manager.pago.infrastructure.controller.dto.TarjetaResponse;
import es.delivery.manager.pago.infrastructure.mapper.TarjetaMapper;
import es.delivery.manager.pago.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tarjetas")
@RequiredArgsConstructor
public class TarjetaController {

    private final GuardarTarjetaUseCase guardarTarjetaUseCase;
    private final ListTarjetasUseCase listTarjetasUseCase;
    private final EliminarTarjetaUseCase eliminarTarjetaUseCase;
    private final TarjetaMapper tarjetaMapper;

    // El cliente guarda una tarjeta nueva para si mismo
    @PostMapping
    public ResponseEntity<TarjetaResponse> guardar(@RequestBody GuardarTarjetaRequest request) {
        TokenClaims caller = RequestSecurityContext.require();
        Tarjeta tarjeta = guardarTarjetaUseCase.guardarTarjeta(caller, request.getNumero(), request.getTitular(),
                request.getMesExpiracion(), request.getAnioExpiracion());
        return ResponseEntity.status(HttpStatus.CREATED).body(tarjetaMapper.toResponse(tarjeta));
    }

    // Mis tarjetas guardadas
    @GetMapping
    public List<TarjetaResponse> listar() {
        TokenClaims caller = RequestSecurityContext.require();
        return listTarjetasUseCase.listMisTarjetas(caller).stream()
                .map(tarjetaMapper::toResponse)
                .toList();
    }

    // Una tarjeta de otro cliente responde 404, no 403 (no confirma que exista)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        TokenClaims caller = RequestSecurityContext.require();
        eliminarTarjetaUseCase.eliminarTarjeta(caller, id);
        return ResponseEntity.noContent().build();
    }
}
