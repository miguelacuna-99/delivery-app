package es.delivery.manager.pago.infrastructure.pasarela;

import es.delivery.manager.pago.domain.service.PasarelaPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Mock de pasarela de firma: autoriza el cobro con una probabilidad configurable
 * (pago.mock.probabilidad-exito) y genera una "firma" SHA-256 del cargo,
 * imitando el resguardo que devolveria una pasarela real.
 */
@Slf4j
@Component
public class MockPasarelaAdapter implements PasarelaPort {

    private final double probabilidadExito;
    private final SecureRandom random = new SecureRandom();

    public MockPasarelaAdapter(@Value("${pago.mock.probabilidad-exito:0.9}") double probabilidadExito) {
        this.probabilidadExito = probabilidadExito;
    }

    @Override
    public ResultadoCobro cobrar(String pedidoId, BigDecimal importe) {
        boolean autorizado = random.nextDouble() < probabilidadExito;
        String firma = firmar(pedidoId + "|" + importe + "|" + Instant.now());
        log.info("Cobro simulado de {} para pedido {}: {}", importe, pedidoId,
                autorizado ? "AUTORIZADO" : "DENEGADO");
        return new ResultadoCobro(autorizado, firma);
    }

    private String firmar(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(contenido.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
