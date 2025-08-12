package gotaxi.uberlikeappbackend.modules.auth.domain.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

public interface OtpRateLimiterPort {
    Mono<Void> hit(String key, int limit, Duration window, String errorMsg);
    Mono<Void> reset(String key);
}