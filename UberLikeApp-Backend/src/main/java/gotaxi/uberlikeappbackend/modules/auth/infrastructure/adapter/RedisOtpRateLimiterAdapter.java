package gotaxi.uberlikeappbackend.modules.auth.infrastructure.adapter;

import gotaxi.uberlikeappbackend.modules.auth.domain.exception.AuthException;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.OtpRateLimiterPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisOtpRateLimiterAdapter implements OtpRateLimiterPort {

    private final ReactiveStringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> hit(String rateLimitKey, int maxAttempts, Duration window, String errorMessage) {
        return redisTemplate.opsForValue()
                .increment(rateLimitKey)
                .flatMap(currentCount -> {
                    if (currentCount == 1) {
                        // Primer intento: establecer TTL
                        return redisTemplate.expire(rateLimitKey, window).thenReturn(currentCount);
                    }
                    return Mono.just(currentCount);
                })
                .flatMap(currentCount -> {
                    if (currentCount > maxAttempts) {
                        return Mono.error(new AuthException(errorMessage, HttpStatus.TOO_MANY_REQUESTS));
                    }
                    return Mono.empty();
                });
    }

    @Override
    public Mono<Void> reset(String rateLimitKey) {
        return redisTemplate.delete(rateLimitKey).then();
    }
}
