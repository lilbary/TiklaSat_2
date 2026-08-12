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
@Table(name = "bids")
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Hangi açık artırmaya teklif veriliyor?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    // Teklifi kim veriyor?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    // Teklif Miktarı (Para olduğu için yine BigDecimal)
    // precision/scale EXPLICIT: migration'da NUMERIC(15,2) yazacağız,
    // Hibernate'in varsayımıyla (NUMERIC(19,2)) çakışmasın.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    // Teklifin verildiği an
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
