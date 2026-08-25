package es.delivery.manager.comercio.infrastructure.controller;

import es.delivery.manager.comercio.application.service.CifAlreadyExistsException;
import es.delivery.manager.comercio.application.service.ComercioNotFoundException;
import es.delivery.manager.comercio.application.service.ForbiddenOperationException;
import es.delivery.manager.comercio.application.service.ProductoNotFoundException;
import es.delivery.manager.comercio.application.service.ValorPuntoInvalidoException;
import es.delivery.manager.comercio.infrastructure.security.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ComercioNotFoundException.class, ProductoNotFoundException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CifAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleCifAlreadyExists(CifAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ValorPuntoInvalidoException.class)
    public ResponseEntity<Map<String, String>> handleValorPuntoInvalido(ValorPuntoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(ForbiddenOperationException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }
}
