package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.PasswordResetToken;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.PasswordResetTokenRepository;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository.PasswordResetTokenRepositoryR2dbc; // <-- Importa la nueva interfaz R2DBC
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenRepository {

    private final PasswordResetTokenRepositoryR2dbc r2dbcRepo;

    @Override
    public Mono<PasswordResetToken> save(PasswordResetToken token) {
        return r2dbcRepo.save(token);
    }
    @Override
    public Mono<PasswordResetToken> findByToken(String token) {
        return r2dbcRepo.findByToken(token);
    }
    @Override
    public Mono<Void> deleteByUserId(Long userId) { // <-- userId es Long
        return r2dbcRepo.deleteByUserId(userId);
    }
    @Override
    public Mono<PasswordResetToken> findLatestByUserId(Long userId) {
        return r2dbcRepo.findLatestByUserId(userId);
    }
}