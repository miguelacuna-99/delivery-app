package es.delivery.manager.fidelidad.infrastructure.controller;

import es.delivery.manager.fidelidad.application.service.CodigoCuponDuplicadoException;
import es.delivery.manager.fidelidad.application.service.CuponNotFoundException;
import es.delivery.manager.fidelidad.application.service.ForbiddenOperationException;
import es.delivery.manager.fidelidad.infrastructure.security.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CuponNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(CuponNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CodigoCuponDuplicadoException.class)
    public ResponseEntity<Map<String, String>> handleDuplicado(CodigoCuponDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
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
