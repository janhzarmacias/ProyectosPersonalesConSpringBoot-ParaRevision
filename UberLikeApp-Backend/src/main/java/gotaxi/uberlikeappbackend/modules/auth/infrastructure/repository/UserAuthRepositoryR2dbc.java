package gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository;

import gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository.model.UserAuthEntity;
import org.springframework.data.r2dbc.repository.Query; // ¡Importa esta anotación!
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserAuthRepositoryR2dbc extends ReactiveCrudRepository<UserAuthEntity, Long> {
    @Query("SELECT * FROM auth.user_auth WHERE email = :email AND deleted = false")
    Mono<UserAuthEntity> findByEmail(String email);

    @Query("SELECT COUNT(*) > 0 FROM auth.user_auth WHERE email = :email AND is_email_verified = true AND (deleted IS NULL OR deleted = false)")
    Mono<Boolean> existsVerifiedByEmail(String email);

    @Query("SELECT * FROM auth.user_auth WHERE phone_number = :phoneNumber AND deleted = false")
    Mono<UserAuthEntity> findByPhoneNumber(String phoneNumber);


    @Query("SELECT COUNT(*) > 0 FROM auth.user_auth WHERE phone_number = :phoneNumber AND is_phone_number_verified = true AND (deleted IS NULL OR deleted = false)")
    Mono<Boolean> existsVerifiedByPhoneNumber(String phoneNumber);


    @Query("SELECT * FROM auth.user_auth WHERE (email = :identifier OR phone_number = :identifier) AND deleted = false")
    Mono<UserAuthEntity> findByEmailOrPhoneNumber(String identifier);

}