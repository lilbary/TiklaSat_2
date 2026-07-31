package com.gib.tiklasat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Sniper protection (son saniye teklifi uzatması) kayıtlarını tutan entity.
 * Her uzatma için eski ve yeni bitiş zamanlarını kaydeder.
 */
@Entity
@Table(name = "auction_extensions")
@Getter
@Setter
@NoArgsConstructor
public class AuctionExtension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_extensions_auction"))
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bid_id", nullable = false, foreignKey = @ForeignKey(name = "fk_extensions_bid"))
    private Bid bid;

    @Column(name = "extension_no", nullable = false)
    private Short extensionNo;

    @Column(name = "previous_ends_at", nullable = false)
    private Instant previousEndsAt;

    @Column(name = "new_ends_at", nullable = false)
    private Instant newEndsAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
