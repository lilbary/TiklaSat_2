package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "auctions")
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Hangi ilan için açık artırma yapılıyor?
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false, unique = true)
    private Listing listing;

    // Başlangıç Fiyatı (Kuruş/Küsürat hassasiyeti için BigDecimal kullanıyoruz)
    @Column(name = "starting_price", nullable = false)
    private BigDecimal startingPrice;

    // Açık Artırma Başlangıç Zamanı
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    // Açık Artırma Bitiş Zamanı
    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    // Durumu: ACTIVE, ENDED, CANCELLED
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
