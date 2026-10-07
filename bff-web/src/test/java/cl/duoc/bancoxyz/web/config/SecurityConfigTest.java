package cl.duoc.bancoxyz.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

    @Test
    void convertsExternalRealmRoleToSpringAuthority() {
        Instant now = Instant.now();
        Jwt jwt = new Jwt(
                "token",
                now,
                now.plusSeconds(300),
                Map.of("alg", "none"),
                Map.of("sub", "web-user", "realm_access", Map.of("roles", List.of("WEB")))
        );

        var authentication = new SecurityConfig(new ObjectMapper())
                .jwtAuthenticationConverter()
                .convert(jwt);

        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_WEB")));
    }
}
