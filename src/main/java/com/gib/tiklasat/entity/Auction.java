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
    @Column(name = "start_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal startingPrice;

    // Açık Artırma Başlangıç Zamanı — DB kolonu "starts_at" (migration V4)
    @Column(name = "starts_at", nullable = false)
    private Instant startTime;

    // Açık Artırma Bitiş Zamanı — DB kolonu "ends_at" (migration V4)
    @Column(name = "ends_at", nullable = false)
    private Instant endTime;

    // Durumu: ACTIVE, ENDED, CANCELLED
    @Column(length = 20, nullable = false)
    private String status = "ACTIVE";

    // Kazanan: İhale kapandığında en yüksek teklifi veren kullanıcı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "original_ends_at", nullable = false)
    private Instant originalEndsAt;

    @Column(name = "extension_count", nullable = false)
    private int extensionCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
