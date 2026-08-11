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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kullanıcıların açık artırmalara verdiği teklifleri temsil eden entity.
 * Append-only bir tablodur, mevcut bir teklifin tutarı güncellenmez.
 */
@Entity
@Table(name = "bids")
@Getter
@Setter
@NoArgsConstructor
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bids_auction"))
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bids_bidder"))
    private User bidder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voided_by", foreignKey = @ForeignKey(name = "fk_bids_voided_by"))
    private User voidedBy;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "is_proxy", nullable = false)
    private Boolean isProxy = false;

    @Column(name = "status", length = 12, nullable = false)
    private String status = "WINNING";

    @Column(name = "ip_address", columnDefinition = "INET")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "voided_at")
    private Instant voidedAt;

    @Column(name = "void_reason", length = 500)
    private String voidReason;
}
