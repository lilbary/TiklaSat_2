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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * İlanlara ait açık artırma süreçlerini yöneten ana entity.
 * Fiyat, başlangıç/bitiş zamanları ve ihale durumlarını takip eder.
 */
@Entity
@Table(name = "auctions")
@Getter
@Setter
@NoArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_auctions_listing"))
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id", foreignKey = @ForeignKey(name = "fk_auctions_winner"))
    private User winnerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by", foreignKey = @ForeignKey(name = "fk_auctions_cancelled_by"))
    private User cancelledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highest_bid_id", foreignKey = @ForeignKey(name = "fk_auctions_highest_bid"))
    private Bid highestBid;

    @Column(name = "start_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal startPrice;

    @Column(name = "reserve_price", precision = 15, scale = 2)
    private BigDecimal reservePrice;

    @Column(name = "current_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentPrice;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency = "TRY";

    @Column(name = "bid_count", nullable = false)
    private Integer bidCount = 0;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "original_ends_at", nullable = false, updatable = false)
    private Instant originalEndsAt;

    @Column(name = "extension_count", nullable = false)
    private Short extensionCount = 0;

    @Column(name = "status", length = 28, nullable = false)
    private String status = "SCHEDULED";

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
