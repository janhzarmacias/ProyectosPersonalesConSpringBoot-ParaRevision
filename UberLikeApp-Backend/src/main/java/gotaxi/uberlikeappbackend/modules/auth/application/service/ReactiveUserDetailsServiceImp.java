package gotaxi.uberlikeappbackend.modules.auth.application.service; // O un paquete más específico como .security

import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImp implements ReactiveUserDetailsService {

    private final UserAuthRepository userAuthRepository;

    @Override
    public Mono<UserDetails> findByUsername(String identifier) {
        return userAuthRepository.findByEmailOrPhoneNumber(identifier)
                .map(userAuth -> org.springframework.security.core.userdetails.User
                        .withUsername(userAuth.getEmail() != null ? userAuth.getEmail() : userAuth.getPhoneNumber())
                        .password(userAuth.getPasswordHash())
                        .roles(userAuth.getRole().name())
                        .accountExpired(!userAuth.isEnabled())
                        .accountLocked(!userAuth.isEnabled())
                        .credentialsExpired(!userAuth.isEnabled())
                        .disabled(!userAuth.isEnabled())
                        .build()
                )
                .switchIfEmpty(Mono.error(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found with identifier: " + identifier)));
    }
}