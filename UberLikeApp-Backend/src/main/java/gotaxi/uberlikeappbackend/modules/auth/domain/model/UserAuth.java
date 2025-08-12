package gotaxi.uberlikeappbackend.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuth {
    private Long id;
    private String email;
    private String phoneNumber;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    // private boolean verified; // <--- OLD: REMOVE THIS

    // NEW: Granular verification flags
    private boolean isEmailVerified;
    private boolean isPhoneNumberVerified;

    private String verificationCode;
    private LocalDateTime verificationCodeExpiresAt;

    private AuthProvider provider;
    private String providerId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean deleted;

    private boolean termsAccepted;
    private boolean privacyAccepted;
    private String termsVersion;
    private String privacyVersion;
    private LocalDateTime consentAt;
}