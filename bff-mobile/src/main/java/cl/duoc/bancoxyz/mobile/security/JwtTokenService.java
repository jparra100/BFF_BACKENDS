package cl.duoc.bancoxyz.mobile.security;

import cl.duoc.bancoxyz.mobile.config.SecurityProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.time.Instant;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
public class JwtTokenService {

    private final JwtEncoder encoder;
    private final SecurityProperties properties;

    public JwtTokenService(JwtEncoder encoder, SecurityProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public GeneratedToken generate(Authentication authentication) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.tokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(authentication.getName())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("channel", properties.channel())
                .claim("roles", List.of(properties.role()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new GeneratedToken(value, properties.tokenTtl().toSeconds(), properties.channel());
    }

    public record GeneratedToken(String value, long expiresIn, String channel) {
    }
}
