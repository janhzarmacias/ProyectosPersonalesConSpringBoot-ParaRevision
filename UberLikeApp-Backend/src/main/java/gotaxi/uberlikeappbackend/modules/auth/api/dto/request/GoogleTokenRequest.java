package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import lombok.Data;

@Data
public class GoogleTokenRequest {
    private String idToken;       // JWT de Google enviado desde frontend
    private String deviceId;      // identificador único del dispositivo (opcional, pero útil)
    private String userAgent;     // información del agente del cliente ("Android/14 GoApp")
    private boolean termsAccepted;
    private boolean privacyAccepted;
    private String termsVersion;
    private String privacyVersion;
}
