package gotaxi.uberlikeappbackend.modules.auth.infrastructure.repository.model;

import gotaxi.uberlikeappbackend.modules.auth.domain.model.AuthProvider;
import gotaxi.uberlikeappbackend.modules.auth.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor // <--- Needs to be regenerated or manually updated
@Table("auth.user_auth")
public class UserAuthEntity {

    @Id
    private Long id;

    private String email;
    private String phoneNumber;
    private String passwordHash;
    private Role role;
    private boolean enabled;

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