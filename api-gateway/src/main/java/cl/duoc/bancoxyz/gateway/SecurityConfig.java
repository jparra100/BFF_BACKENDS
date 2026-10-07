package cl.duoc.bancoxyz.gateway;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.security.config.web.server.ServerHttpSecurity; import org.springframework.security.web.server.SecurityWebFilterChain;
@Configuration public class SecurityConfig {
    @Bean SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,@Value("${app.security.enabled:false}") boolean enabled){
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        if(enabled){http.authorizeExchange(e->e.pathMatchers("/actuator/health").permitAll().anyExchange().authenticated()).oauth2ResourceServer(o->o.jwt(j->{}));}
        else{http.authorizeExchange(e->e.anyExchange().permitAll());}
        return http.build();
    }
}
