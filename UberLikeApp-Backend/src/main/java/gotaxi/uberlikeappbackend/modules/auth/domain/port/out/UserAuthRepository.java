package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import reactor.core.publisher.Mono;

public interface UserAuthRepository {
    Mono<UserAuth> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<UserAuth> findByPhoneNumber(String phoneNumber);
    Mono<Boolean> existsByPhoneNumber(String phoneNumber);
    Mono<UserAuth> findByEmailOrPhoneNumber(String identifier);
    Mono<UserAuth> save(UserAuth user);
    Mono<UserAuth> findById(Long id); // Ya existe, importante para AddContactInfoService
}