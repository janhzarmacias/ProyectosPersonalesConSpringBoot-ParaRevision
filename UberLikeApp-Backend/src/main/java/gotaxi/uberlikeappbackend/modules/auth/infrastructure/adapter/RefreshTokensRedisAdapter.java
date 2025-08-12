package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gotaxi.uberlikeappbackend.modules.auth.api.dto.response.ActiveSessionResponse;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.RefreshTokenMetadata;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RefreshTokensRedisAdapter implements RefreshTokenStore {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${jwt.refreshExpirationDays}")
    private int refreshExpirationDays;

    private static final String REFRESH_PREFIX = "refresh:";

    private Duration getRefreshTokenTTL() {
        return Duration.ofDays(refreshExpirationDays);
    }

    private static final String REFRESH_INDEX_PREFIX = "refresh:index:"; // índice por dispositivo

    private String buildIndexKey(String identifier, String deviceId, String userAgent) {
        return REFRESH_INDEX_PREFIX + identifier + ":" + userAgent + ":" + deviceId;
    }


    @Override
    public Mono<Void> storeRefreshToken(String token, RefreshTokenMetadata metadata) {
        try {
            String json = objectMapper.writeValueAsString(metadata);
            String redisKey = REFRESH_PREFIX + token;
            String indexKey = buildIndexKey(metadata.getIdentifier(), metadata.getDeviceId(), metadata.getUserAgent());

            return redisTemplate.opsForValue()
                    .get(indexKey)
                    .flatMap(previousTokenKey -> {
                        if (previousTokenKey != null) {
                            return redisTemplate.delete(REFRESH_PREFIX + previousTokenKey);
                        }
                        return Mono.empty();
                    })
                    .then(redisTemplate.opsForValue().set(redisKey, json, getRefreshTokenTTL()))
                    .then(redisTemplate.opsForValue().set(indexKey, token, getRefreshTokenTTL()))
                    .then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error serializing refresh token metadata", e));
        }
    }

    @Override
    public Mono<RefreshTokenMetadata> getMetadataFromRefreshToken(String token) {
        return redisTemplate.opsForValue()
                .get(REFRESH_PREFIX + token)
                .flatMap(json -> {
                    try {
                        RefreshTokenMetadata metadata = objectMapper.readValue(json, RefreshTokenMetadata.class);
                        return Mono.just(metadata);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException("Error deserializing refresh token metadata", e));
                    }
                });
    }

    @Override
    public Mono<Void> deleteRefreshToken(String token) {
        return getMetadataFromRefreshToken(token)
                .flatMap(metadata -> {
                    String indexKey = buildIndexKey(metadata.getIdentifier(), metadata.getDeviceId(), metadata.getUserAgent());
                    return redisTemplate.delete(
                            Flux.fromIterable(List.of(REFRESH_PREFIX + token, indexKey))
                    ).then();
                })
                .onErrorResume(e ->
                        redisTemplate.delete(REFRESH_PREFIX + token).then()
                );
    }

    @Override
    public Mono<Void> deleteByIdentifierAndDevice(String identifier, String deviceId, String userAgent) {
        String indexKey = buildIndexKey(identifier, deviceId, userAgent);
        return redisTemplate.opsForValue()
                .get(indexKey)
                .flatMap(token -> {
                    if (token == null) return Mono.empty();
                    return redisTemplate.delete(Flux.fromIterable(List.of(indexKey, REFRESH_PREFIX + token)))
                            .then();
                });
    }

    @Override
    public Mono<Void> deleteAllByIdentifier(String identifier) {
        String pattern = REFRESH_INDEX_PREFIX + identifier + ":";
        return redisTemplate.scan()
                .filter(key -> key.startsWith(pattern))
                .flatMap(indexKey -> redisTemplate.opsForValue()
                        .get(indexKey)
                        .flatMap(token ->
                                redisTemplate.delete(Flux.fromIterable(List.of(indexKey, REFRESH_PREFIX + token)))
                        )
                )
                .then();
    }


    @Override
    public Mono<String> getRefreshTokenByIndex(String identifier, String deviceId, String userAgent) {
        String indexKey = buildIndexKey(identifier, deviceId, userAgent);
        return redisTemplate.opsForValue().get(indexKey);
    }

    @Override
    public Mono<Void> deleteKeys(String... keys) {
        return redisTemplate.delete(Flux.fromArray(keys)).then();
    }

    @Override
    public Mono<List<ActiveSessionResponse>> getAllSessionsByIdentifier(String identifier) {
        String pattern = REFRESH_INDEX_PREFIX + identifier + ":";

        return redisTemplate.scan()
                .filter(key -> key.startsWith(pattern))
                .flatMap(indexKey -> redisTemplate.opsForValue()
                        .get(indexKey)
                        .flatMap(token -> getMetadataFromRefreshToken(token)
                                .map(metadata -> new ActiveSessionResponse(
                                        metadata.getIdentifier(),
                                        metadata.getDeviceId(),
                                        metadata.getUserAgent(),
                                        metadata.getCreatedAt()
                                ))
                        )
                )
                .collectList();
    }

    @Override
    public Flux<RefreshTokenMetadata> getAllMetadataByIdentifier(String identifier) {
        String pattern = REFRESH_INDEX_PREFIX + identifier + ":";

        return redisTemplate.scan()
                .filter(key -> key.startsWith(pattern))
                .flatMap(indexKey -> redisTemplate.opsForValue()
                        .get(indexKey)
                        .flatMap(jsonToken ->
                                redisTemplate.opsForValue().get(REFRESH_PREFIX + jsonToken)
                                        .flatMap(json -> {
                                            try {
                                                RefreshTokenMetadata metadata = objectMapper.readValue(json, RefreshTokenMetadata.class);
                                                return Mono.just(metadata);
                                            } catch (JsonProcessingException e) {
                                                return Mono.empty(); // o loggear el error
                                            }
                                        })
                        )
                );
    }





}
