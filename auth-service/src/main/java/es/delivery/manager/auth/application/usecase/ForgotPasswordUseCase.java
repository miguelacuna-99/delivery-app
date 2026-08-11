package es.delivery.manager.auth.application.usecase;

public interface ForgotPasswordUseCase {
    void requestReset(String mail);
}
