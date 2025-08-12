package gotaxi.uberlikeappbackend.modules.auth.infrastructure.jwt;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthWebFilter extends AuthenticationWebFilter {

    public JwtAuthWebFilter(JwtService jwtService, ReactiveUserDetailsService userDetailsService) {
        super(new JwtReactiveAuthenticationManager(jwtService, userDetailsService));
        setServerAuthenticationConverter(new JwtServerAuthenticationConverter(jwtService));
    }

}
