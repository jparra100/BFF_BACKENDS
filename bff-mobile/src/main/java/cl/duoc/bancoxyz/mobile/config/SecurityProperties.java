package cl.duoc.bancoxyz.mobile.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        String username,
        String password,
        String jwtSecretBase64,
        String issuer,
        String channel,
        String role,
        Duration tokenTtl
) {
}
