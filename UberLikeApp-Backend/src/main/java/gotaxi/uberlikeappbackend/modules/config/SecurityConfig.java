package gotaxi.uberlikeappbackend.modules.config;

import gotaxi.uberlikeappbackend.modules.auth.infrastructure.jwt.JwtAuthWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
public class SecurityConfig {

    private final JwtAuthWebFilter jwtAuthWebFilter;

    public SecurityConfig(JwtAuthWebFilter jwtAuthWebFilter) {
        this.jwtAuthWebFilter = jwtAuthWebFilter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(csrfSpec -> csrfSpec.disable())
                .cors(corsSpec -> corsSpec.disable()) // O configura CORS si usas frontend aparte
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(authz -> authz
                        .pathMatchers(
                                "/api/auth/**",
                                "/swagger-ui.html",
                                "/api/session-events/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/ws/**" // ✅ permite acceso a WebSockets
                        ).permitAll()

                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtAuthWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
