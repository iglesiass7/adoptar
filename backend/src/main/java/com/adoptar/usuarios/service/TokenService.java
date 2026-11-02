package com.adoptar.usuarios.service;

import com.adoptar.usuarios.dominio.Usuario;
import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Genera el token que identifica al usuario en cada pedido, sin volver a pedirle la contraseña.
 * <p>
 * Lleva adentro el id (sub), el mail y el rol, firmados: el servidor no guarda sesiones.
 */
@Service
public class TokenService {

    public static final String EMISOR = "adoptar";
    public static final Duration DURACION = Duration.ofHours(8);

    private final JwtEncoder encoder;

    public TokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public Token generar(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(DURACION);

        JwtClaimsSet datos = JwtClaimsSet.builder()
                .issuer(EMISOR)
                .subject(usuario.getId().toString())
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol().name())
                .build();

        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        String valor = encoder.encode(JwtEncoderParameters.from(cabecera, datos)).getTokenValue();
        return new Token(valor, expira);
    }

    public record Token(String valor, Instant expiraEn) {
    }
}
