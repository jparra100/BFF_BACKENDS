package cl.duoc.bancoxyz.atm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    @Test
    void convertsExternalAtmRoleAndChannelAuthorities() {
        Instant now = Instant.now();
        Jwt jwt = new Jwt(
                "token", now, now.plusSeconds(300), Map.of("alg", "none"),
                Map.of("sub", "atm-user", "realm_access", Map.of("roles", List.of("ATM")))
        );

        var authentication = new SecurityConfig(new ObjectMapper())
                .jwtAuthenticationConverter()
                .convert(jwt);

        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ATM", "CHANNEL_ATM");
    }
}
