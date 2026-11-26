package com.xochitepec.patrimonio.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.xochitepec.patrimonio.usuario.Usuario;

@Service
public class JwtService {

    public static final String ISSUER = "patrimonio-xochitepec";

    private final JwtEncoder encoder;
    private final Duration expiracion;

    public JwtService(JwtEncoder encoder, @Value("${app.jwt.expiration-minutes}") long minutos) {
        this.encoder = encoder;
        this.expiracion = Duration.ofMinutes(minutos);
    }

    public Token emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        Instant expira = ahora.plus(expiracion);
        // Solo el id es autoritativo: rol y área se recargan de la BD en cada petición
        // (JwtUsuarioConverter), así un cambio de rol, área o desactivación aplica al instante.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(String.valueOf(usuario.getId()))
                .issuedAt(ahora)
                .expiresAt(expira)
                .claim("rol", usuario.getRol().getNombre().name())
                .build();
        String valor = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new Token(valor, expira);
    }

    public record Token(String valor, Instant expira) {
    }
}
