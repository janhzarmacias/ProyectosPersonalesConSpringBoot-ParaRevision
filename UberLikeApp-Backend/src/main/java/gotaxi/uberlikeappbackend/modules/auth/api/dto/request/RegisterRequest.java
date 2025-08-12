package gotaxi.uberlikeappbackend.modules.auth.api.dto.request;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class RegisterRequest {
    private String identifier; // Puede ser email o phone
    private String password;

    @AssertTrue(message = "You must accept the Terms of Use.")
    private boolean termsAccepted;

    @AssertTrue(message = "You must accept the Privacy Policy.")
    private boolean privacyAccepted;

    //versión del documento aceptado (útil para auditoría)
    private String termsVersion;   // ej: "v1.3"
    private String privacyVersion; // ej: "v2.0"
}
