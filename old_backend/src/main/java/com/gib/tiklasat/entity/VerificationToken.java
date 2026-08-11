package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Doğrulama token'ları (BR-U-004).
 * E-posta doğrulama, şifre sıfırlama, telefon doğrulama
 * gibi tek kullanımlık token'lar için tek tablo.
 * Purpose alanı ile ayrışır.
 */
@Entity
@Table(name = "verification_tokens")
@Getter
@Setter
@NoArgsConstructor
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_verification_tokens_user"))
    private User user;

    /**
     * Token amacı: EMAIL_VERIFICATION, PASSWORD_RESET, PHONE_VERIFICATION
     * CHECK kısıtı ile veritabanında da zorlanır.
     */
    @Column(name = "purpose", nullable = false, length = 24)
    private String purpose;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Token kullanıldıysa zaman damgası. NULL = henüz kullanılmadı. */
    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Yardımcı metotlar ────────────────────────────

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public boolean isUsable() {
        return !isExpired() && !isConsumed();
    }
}
