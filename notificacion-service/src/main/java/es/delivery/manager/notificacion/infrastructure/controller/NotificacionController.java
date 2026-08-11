package es.delivery.manager.notificacion.infrastructure.controller;

import es.delivery.manager.notificacion.application.usecase.ListNotificacionesUseCase;
import es.delivery.manager.notificacion.application.usecase.MarcarLeidaUseCase;
import es.delivery.manager.notificacion.domain.model.TipoNotificacion;
import es.delivery.manager.notificacion.domain.model.TokenClaims;
import es.delivery.manager.notificacion.infrastructure.controller.dto.NotificacionResponse;
import es.delivery.manager.notificacion.infrastructure.mapper.NotificacionMapper;
import es.delivery.manager.notificacion.infrastructure.security.RequestSecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final ListNotificacionesUseCase listNotificacionesUseCase;
    private final MarcarLeidaUseCase marcarLeidaUseCase;
    private final NotificacionMapper notificacionMapper;

    // Bandeja del comercio: ?tipo=PEDIDO_PENDIENTE | PEDIDO_PAGADO (solo sin leer)
    @GetMapping
    public List<NotificacionResponse> listNoLeidas(@RequestParam TipoNotificacion tipo) {
        TokenClaims caller = RequestSecurityContext.require();
        return listNotificacionesUseCase.listNoLeidas(caller, tipo).stream()
                .map(notificacionMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/leida")
    public NotificacionResponse marcarLeida(@PathVariable String id) {
        TokenClaims caller = RequestSecurityContext.require();
        return notificacionMapper.toResponse(marcarLeidaUseCase.marcarLeida(caller, id));
    }
}
