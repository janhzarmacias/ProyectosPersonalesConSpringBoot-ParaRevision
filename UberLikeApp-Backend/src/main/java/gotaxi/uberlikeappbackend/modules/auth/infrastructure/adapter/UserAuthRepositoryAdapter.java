package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.UserAuthRepository;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.UserAuth;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository.model.UserAuthEntity;
import gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository.UserAuthRepositoryR2dbc;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserAuthRepositoryAdapter implements UserAuthRepository {

    private final UserAuthRepositoryR2dbc r2dbcRepo;

    @Override
    public Mono<UserAuth> findByEmail(String email) {
        return r2dbcRepo.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Mono<UserAuth> save(UserAuth user) {
        return r2dbcRepo.save(toEntity(user)).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return r2dbcRepo.existsVerifiedByEmail(email);
    }

    @Override
    public Mono<UserAuth> findByPhoneNumber(String phoneNumber) {
        return r2dbcRepo.findByPhoneNumber(phoneNumber).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByPhoneNumber(String phoneNumber) {
        return r2dbcRepo.existsVerifiedByPhoneNumber(phoneNumber);
    }

    @Override
    public Mono<UserAuth> findByEmailOrPhoneNumber(String identifier) {
        return r2dbcRepo.findByEmailOrPhoneNumber(identifier)
                .map(this::toDomain);
    }

    @Override
    public Mono<UserAuth> findById(Long id) {
        return r2dbcRepo.findById(id).map(this::toDomain);
    }

    private UserAuth toDomain(UserAuthEntity entity) {
        return new UserAuth(
                entity.getId(),
                entity.getEmail(),
                entity.getPhoneNumber(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isEnabled(),
                entity.isEmailVerified(),
                entity.isPhoneNumberVerified(),
                entity.getVerificationCode(),
                entity.getVerificationCodeExpiresAt(),
                entity.getProvider(),
                entity.getProviderId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.isDeleted(),
                entity.isTermsAccepted(),
                entity.isPrivacyAccepted(),
                entity.getTermsVersion(),
                entity.getPrivacyVersion(),
                entity.getConsentAt()
        );
    }

    private UserAuthEntity toEntity(UserAuth user) {
        return new UserAuthEntity(
                user.getId(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getPasswordHash(),
                user.getRole(),
                user.isEnabled(),
                user.isEmailVerified(),
                user.isPhoneNumberVerified(),
                user.getVerificationCode(),
                user.getVerificationCodeExpiresAt(),
                user.getProvider(),
                user.getProviderId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.isDeleted(),
                user.isTermsAccepted(),
                user.isPrivacyAccepted(),
                user.getTermsVersion(),
                user.getPrivacyVersion(),
                user.getConsentAt()
        );
    }
}