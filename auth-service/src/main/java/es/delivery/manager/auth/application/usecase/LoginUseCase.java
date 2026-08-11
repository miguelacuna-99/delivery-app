package es.delivery.manager.auth.application.usecase;

import es.delivery.manager.auth.application.service.LoginResult;

public interface LoginUseCase {
    LoginResult login(String username, String rawPassword);
}
