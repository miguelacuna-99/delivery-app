package es.delivery.manager.pago.infrastructure.controller;

import es.delivery.manager.pago.application.service.ForbiddenOperationException;
import es.delivery.manager.pago.application.service.TarjetaInvalidaException;
import es.delivery.manager.pago.application.service.TarjetaNotFoundException;
import es.delivery.manager.pago.infrastructure.security.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TarjetaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleTarjetaNotFound(TarjetaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TarjetaInvalidaException.class)
    public ResponseEntity<Map<String, String>> handleTarjetaInvalida(TarjetaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
