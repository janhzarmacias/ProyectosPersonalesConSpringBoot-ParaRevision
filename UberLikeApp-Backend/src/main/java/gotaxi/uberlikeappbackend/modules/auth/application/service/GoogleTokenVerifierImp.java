package gotaxi.uberlikeappbackend.modules.auth.application.service; // Or a dedicated infrastructure package

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory; // Using Gson for JSON parsing

import gotaxi.uberlikeappbackend.modules.auth.api.dto.external.GoogleUserInfo;
import gotaxi.uberlikeappbackend.modules.auth.domain.port.out.GoogleTokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifierImp implements GoogleTokenVerifier {

    private final GoogleIdTokenVerifier verifier;

    // You need to configure your Google Client ID in application.properties/yml
    public GoogleTokenVerifierImp(@Value("${google.client.id}") String googleClientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(googleClientId))
                .build();
    }

    @Override
    public Mono<GoogleUserInfo> verify(String idToken) {
        return Mono.fromCallable(() -> {
            try {
                GoogleIdToken googleIdToken = verifier.verify(idToken);
                if (googleIdToken != null) {
                    GoogleIdToken.Payload payload = googleIdToken.getPayload();
                    // Extract user info
                    GoogleUserInfo userInfo = new GoogleUserInfo();
                    userInfo.setId(payload.getSubject()); // 'sub' is the unique user ID from Google
                    userInfo.setEmail(payload.getEmail());
                    userInfo.setName((String) payload.get("name"));
                    userInfo.setPicture((String) payload.get("picture"));
                    return userInfo;
                } else {
                    throw new IllegalArgumentException("Invalid Google ID Token.");
                }
            } catch (GeneralSecurityException | IOException e) {
                // Log the exception for debugging
                System.err.println("Error verifying Google ID Token: " + e.getMessage());
                throw new RuntimeException("Failed to verify Google ID Token", e);
            }
        });
    }
}