package cl.duoc.bancoxyz.payments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,
                                    @Value("${app.security.enabled:false}") boolean enabled) throws Exception {
        http.csrf(csrf -> csrf.disable());
        if (enabled) {
            http.authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/actuator/health").permitAll()
                            .anyRequest().hasAnyRole("WEB", "ATM", "MOBILE", "ADMIN"))
                    .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                            jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        } else {
            http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        }
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> realmRoles(jwt.getClaim("realm_access")));
        return converter;
    }

    private Collection<GrantedAuthority> realmRoles(Map<String, Object> realmAccess) {
        if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
            return List.of();
        }
        return roles.stream().map(String::valueOf)
                .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role)).toList();
    }
}
