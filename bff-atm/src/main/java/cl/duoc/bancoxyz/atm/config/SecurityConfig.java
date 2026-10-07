package cl.duoc.bancoxyz.atm.config;

import cl.duoc.bancoxyz.atm.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> atmAccess = AuthorizationManagers.allOf(
                AuthorityAuthorizationManager.hasAuthority("ROLE_ATM"),
                AuthorityAuthorizationManager.hasAuthority("CHANNEL_ATM"));

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/auth/token").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/atm/**").access(atmAccess)
                        .anyRequest().denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint((request, response, ex) -> writeSecurityError(
                                response,
                                request.getRequestURI(),
                                HttpStatus.UNAUTHORIZED,
                                "Token ausente o inválido")))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler((request, response, ex) -> writeSecurityError(
                                response,
                                request.getRequestURI(),
                                HttpStatus.FORBIDDEN,
                                "El token no tiene permiso para este canal")))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    UserDetailsService userDetailsService(SecurityProperties properties, PasswordEncoder passwordEncoder) {
        var user = User.withUsername(properties.username())
                .password(passwordEncoder.encode(properties.password()))
                .roles(properties.role())
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    SecretKey jwtSecretKey(SecurityProperties properties) {
        byte[] keyBytes = Base64.getDecoder().decode(properties.jwtSecretBase64());
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET_BASE64 debe contener al menos 32 bytes");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    @Bean
    @ConditionalOnProperty(name = "app.security.mode", havingValue = "local", matchIfMissing = true)
    JwtDecoder jwtDecoder(SecretKey secretKey, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.issuer()));
        return decoder;
    }

    Converter<Jwt, JwtAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();
            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            }
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.get("roles") instanceof List<?> realmRoles) {
                realmRoles.stream()
                        .map(String::valueOf)
                        .forEach(role -> {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                            if ("ATM".equals(role)) {
                                authorities.add(new SimpleGrantedAuthority("CHANNEL_ATM"));
                            }
                        });
            }
            String channel = jwt.getClaimAsString("channel");
            if (channel != null) {
                authorities.add(new SimpleGrantedAuthority("CHANNEL_" + channel));
            }
            return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
        };
    }

    private void writeSecurityError(
            HttpServletResponse response,
            String path,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        ));
    }
}
