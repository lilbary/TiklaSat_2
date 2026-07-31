package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Platform kullanıcıları.
 * <ul>
 *   <li>Fiziksel silme yapılmaz; BR-K-003 uyarınca anonimleştirilir.</li>
 *   <li>Parola Argon2id ile hash'lenir (BR-S-001).</li>
 *   <li>Aynı kullanıcı hem alıcı hem satıcı olabilir (BR-U-003).</li>
 *   <li>@Version ile optimistik kilit — teklif dışı güncellemelerde çakışma koruması.</li>
 * </ul>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    /**
     * UUIDv7 (zaman sıralı). Veritabanı tarafında gen_random_uuid() ile üretilir.
     * Şimdilik UUID v4 kullanıyoruz, ileride UUIDv7 generator'a geçilebilir.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false)
    private UUID id;

    // ── Kimlik ────────────────────────────────────────

    /** E-posta. PostgreSQL CITEXT tipi — büyük/küçük harf duyarsız (BR-U-001) */
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    /** Argon2id hash çıktısı (BR-S-001). Asla düz metin saklanmaz. */
    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Column(name = "password_algo", nullable = false, length = 20)
    private String passwordAlgo = "ARGON2ID";

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    // ── Doğrulama (BR-U-004, BR-U-005) ───────────────

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

    // ── Durum (BR-U-007) ─────────────────────────────

    /**
     * Olası değerler: PENDING_VERIFICATION, ACTIVE, SUSPENDED, BANNED, ANONYMIZED
     * CHECK kısıtı ile veritabanında da zorlanır.
     */
    @Column(name = "status", nullable = false, length = 24)
    private String status = "PENDING_VERIFICATION";

    // ── Konum ────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id",
                foreignKey = @ForeignKey(name = "fk_users_city"))
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id",
                foreignKey = @ForeignKey(name = "fk_users_district"))
    private District district;

    // ── Denormalize puan özeti ────────────────────────

    /** Ortalama puan (0.00 – 5.00). Kaynak: user_ratings tablosu. */
    @Column(name = "rating_avg", precision = 3, scale = 2)
    private BigDecimal ratingAvg;

    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount = 0;

    // ── KVKK rıza kaydı (BR-K-006) ───────────────────

    @Column(name = "consent_version", length = 10)
    private String consentVersion;

    @Column(name = "consent_at")
    private Instant consentAt;

    @Column(name = "consent_ip", columnDefinition = "INET")
    private String consentIp;

    // ── Anonimleştirme (BR-K-003) ────────────────────

    @Column(name = "anonymized_at")
    private Instant anonymizedAt;

    // ── Oturum güvenliği ─────────────────────────────

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    // ── Zaman damgaları ──────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * Optimistik kilit versiyonu.
     * Teklif dışı güncellemelerde (profil düzenleme, durum değişikliği vb.)
     * iki eşzamanlı isteğin birbirini ezmesini önler.
     *
     * NOT: Teklif yolunda pesimistik kilit kullanılır (ADR-0004),
     * bu @Version teklif yolunda devreye girmez.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    // ── İlişkiler ────────────────────────────────────

    /**
     * Kullanıcının rolleri (BUYER, SELLER, ADMIN, MODERATOR).
     * Çoktan-çoğa ilişki, user_roles ara tablosu üzerinden.
     * Eager yerine Lazy yükleme — roller yalnızca istendiğinde çekilir.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
