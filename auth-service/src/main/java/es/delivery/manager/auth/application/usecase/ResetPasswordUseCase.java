package es.delivery.manager.auth.application.usecase;

public interface ResetPasswordUseCase {
    void reset(String rawToken, String nuevaPassword);
}
