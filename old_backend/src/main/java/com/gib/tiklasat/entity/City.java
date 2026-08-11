package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Türkiye illeri.
 * PK = plaka kodu (1–81). Seed verisi V6 migration'da yüklenir.
 * İlgili kural: BR-L-005
 */
@Entity
@Table(name = "cities")
@Getter
@Setter
@NoArgsConstructor
public class City {

    /**
     * Plaka kodu. Otomatik üretilmez, seed data ile gelir.
     * SQL: SMALLINT PRIMARY KEY — CHECK (id BETWEEN 1 AND 81)
     */
    @Id
    @Column(name = "id")
    private Short id;

    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;
}
