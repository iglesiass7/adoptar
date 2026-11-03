package com.adoptar.usuarios.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Fabrica y valida los tokens JWT que entrega el login.
 * <p>
 * Se firman con HS256 y una clave secreta: quien no la tiene no puede fabricar un token válido
 * ni modificar uno (por ejemplo, cambiarle el rol). El valor por defecto es solo para desarrollo:
 * en producción se define la variable de entorno JWT_SECRETO.
 */
@Configuration
public class JwtConfig {

    private final SecretKey clave;

    public JwtConfig(
            @Value("${JWT_SECRETO:clave-de-desarrollo-adoptar-no-usar-en-produccion-2026}") String secreto) {
        // HS256 exige una clave de al menos 256 bits (32 bytes)
        if (secreto.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRETO tiene que tener al menos 32 caracteres");
        }
        this.clave = new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(clave));
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(clave).macAlgorithm(MacAlgorithm.HS256).build();
    }
}
