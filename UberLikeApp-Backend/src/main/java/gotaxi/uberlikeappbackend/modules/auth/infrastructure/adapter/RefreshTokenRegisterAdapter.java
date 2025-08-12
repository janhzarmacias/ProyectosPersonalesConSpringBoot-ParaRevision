package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenRegisterPort;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRegisterAdapter implements RefreshTokenRegisterPort {

    private final RefreshTokenStore refreshTokenStore;

    @Value("${jwt.refreshSecret}")
    private String SECRET_KEY;

    @Override
    public Mono<String> registerRefreshToken(RefreshTokenMetadata metadata) {
        String rawData = UUID.randomUUID() + ":" + metadata.getIdentifier() + ":" + Instant.now();

        return Mono.fromCallable(() -> generateHmacSha256(rawData, SECRET_KEY))
                .map(Base64.getUrlEncoder()::encodeToString)
                .flatMap(token -> refreshTokenStore.storeRefreshToken(token, metadata)
                        .thenReturn(token)
                );
    }

    private byte[] generateHmacSha256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<String> getRefreshTokenByIndex(String identifier, String deviceId, String userAgent) {
        return refreshTokenStore.getRefreshTokenByIndex(identifier, deviceId, userAgent);
    }

    @Override
    public Mono<Void> deleteKeys(String... keys) {
        return refreshTokenStore.deleteKeys(keys);
    }


}
