package gotaxi.uberlikeappbackend.modules.auth.domain.port.in;
import reactor.core.publisher.Mono;

public interface RegisterUserUseCase {
    Mono<Void> register(String identifier, String password,
                        boolean termsAccepted, boolean privacyAccepted,
                        String termsVersion, String privacyVersion);
    Mono<Void> verifyCode(String identifier, String code);

}