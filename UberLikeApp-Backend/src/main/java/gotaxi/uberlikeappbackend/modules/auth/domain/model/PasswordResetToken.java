package gotaxi.uberlikeappbackend.modules.auth.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("auth.password_reset_tokens") // Mapea esta clase a la tabla 'password_reset_tokens' en el esquema 'auth'
public class PasswordResetToken {

    @Id
    @Column("id")
    private Long id;
    @Column("token")
    private String token;
    @Column("user_id")
    private Long userId;
    @Column("expires_at")
    private LocalDateTime expiresAt;
    @Column("created_at")
    private LocalDateTime createdAt;
    @Column("is_used")
    private boolean used;

    private int attempts;

    public PasswordResetToken(String token, Long userId, LocalDateTime expiresAt) { // <-- CAMBIO: userId a Long
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        this.used = false;
        this.attempts = 0;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}