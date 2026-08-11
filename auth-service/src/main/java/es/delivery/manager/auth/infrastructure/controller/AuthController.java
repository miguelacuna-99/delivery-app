package es.delivery.manager.auth.infrastructure.controller;

import es.delivery.manager.auth.application.service.LoginResult;
import es.delivery.manager.auth.application.usecase.ForgotPasswordUseCase;
import es.delivery.manager.auth.application.usecase.LoginUseCase;
import es.delivery.manager.auth.application.usecase.ResetPasswordUseCase;
import es.delivery.manager.auth.application.usecase.ValidateTokenUseCase;
import es.delivery.manager.auth.domain.model.TokenClaims;
import es.delivery.manager.auth.infrastructure.controller.dto.*;
import es.delivery.manager.auth.infrastructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final ValidateTokenUseCase validateTokenUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final UsuarioMapper usuarioMapper;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResult result = loginUseCase.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(LoginResponse.of(result));
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidateResponse> validate(@RequestBody ValidateRequest request) {
        TokenClaims claims = validateTokenUseCase.validate(request.getToken());
        return ResponseEntity.ok(usuarioMapper.toValidateResponse(claims));
    }

    // Responde siempre 204: no revela si el mail existe (evita enumeracion de cuentas)
    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        forgotPasswordUseCase.requestReset(request.getMail());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        resetPasswordUseCase.reset(request.getToken(), request.getNuevaPassword());
        return ResponseEntity.noContent().build();
    }
}
