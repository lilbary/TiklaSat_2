package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Kullanıcı-Rol ara tablosu (BR-U-003).
 * Bileşik PK: (user_id, role_id).
 *
 * Not: User entity'de @ManyToMany ile roller zaten çekilebilir.
 * Bu entity, granted_at ve granted_by gibi ek alanlara erişim gerektiğinde kullanılır.
 */
@Entity
@Table(name = "user_roles")
@IdClass(UserRole.UserRoleId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserRole {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "role_id")
    private Short roleId;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    /** Rolü kim verdi? Admin ise admin'in user_id'si. */
    @Column(name = "granted_by")
    private UUID grantedBy;

    // ── İlişkiler (okuma kolaylığı için) ─────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    // ── Bileşik PK sınıfı ───────────────────────────

    /**
     * JPA bileşik PK için gerekli.
     * Serializable olmalı, equals/hashCode override edilmeli.
     */
    public static class UserRoleId implements Serializable {
        private UUID userId;
        private Short roleId;

        public UserRoleId() {}

        public UserRoleId(UUID userId, Short roleId) {
            this.userId = userId;
            this.roleId = roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserRoleId that)) return false;
            return Objects.equals(userId, that.userId)
                && Objects.equals(roleId, that.roleId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, roleId);
        }
    }
}
