package gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.PasswordResetToken;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PasswordResetTokenRepositoryR2dbc extends R2dbcRepository<PasswordResetToken, Long> { // <-- ID de PasswordResetToken es Long

    Mono<PasswordResetToken> findByToken(String token);

    @Query("DELETE FROM auth.password_reset_tokens WHERE user_id = :userId")
    Mono<Void> deleteByUserId(Long userId);

    @Query(""" 
           SELECT * 
           FROM auth.password_reset_tokens 
           WHERE user_id = :userId 
           ORDER BY created_at DESC 
           LIMIT 1
           """)
    Mono<PasswordResetToken> findLatestByUserId(Long userId);

}