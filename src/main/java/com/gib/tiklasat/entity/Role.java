package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Platform rolleri. Sabit 4 adet — seed data ile gelir.
 * BR-U-002: ZİYARETÇİ burada YOKTUR.
 * Ziyaretçi kimlik doğrulaması yapılmamış istektir; veri değildir.
 *
 * Roller: BUYER (1), SELLER (2), MODERATOR (3), ADMIN (4)
 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @Column(name = "id")
    private Short id;

    /**
     * Rol kodu: BUYER, SELLER, ADMIN, MODERATOR
     * Spring Security'de ROLE_BUYER şeklinde kullanılacak.
     */
    @Column(name = "code", nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "description", length = 200)
    private String description;
}
