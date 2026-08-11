package es.delivery.manager.auth.domain.service;

public interface PasswordPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
