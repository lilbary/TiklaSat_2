package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Giriş denemeleri kaydı (BR-S-003 · Kaba kuvvet tespiti).
 * Var olmayan hesap denemeleri de kaydedilir (user_id nullable).
 *
 * BR-K-005: IP adresi kişisel veridir, 6 ay sonra temizlenir.
 * PK: BIGINT IDENTITY — yüksek hacimli, URL'de asla görünmez.
 */
@Entity
@Table(name = "login_attempts")
@Getter
@Setter
@NoArgsConstructor
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Denenen e-posta adresi. Var olmayan hesaplar için de kaydedilir,
     * bu yüzden User ilişkisi yerine String olarak tutulur.
     */
    @Column(name = "email", nullable = false)
    private String email;

    /** Eğer e-posta geçerli bir kullanıcıya aitse onun UUID'si. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
                foreignKey = @ForeignKey(name = "fk_login_attempts_user"))
    private User user;

    @Column(name = "ip_address", nullable = false, columnDefinition = "INET")
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "successful", nullable = false)
    private Boolean successful;

    @Column(name = "attempted_at", nullable = false)
    private Instant attemptedAt = Instant.now();
}
