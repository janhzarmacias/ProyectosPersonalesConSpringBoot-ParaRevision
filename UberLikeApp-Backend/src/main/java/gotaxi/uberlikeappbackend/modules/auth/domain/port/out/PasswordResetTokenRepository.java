package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.PasswordResetToken;
import reactor.core.publisher.Mono;

public interface PasswordResetTokenRepository {
    Mono<PasswordResetToken> save(PasswordResetToken token);
    Mono<PasswordResetToken> findByToken(String token);
    Mono<Void> deleteByUserId(Long userId);
    Mono<PasswordResetToken> findLatestByUserId(Long userId);
}