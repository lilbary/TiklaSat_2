package com.gib.tiklasat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Açık artırma teklif artış dilimlerini temsil eden entity.
 * Her fiyat aralığı için minimum teklif artış miktarını belirler (Sabit veya Yüzdesel).
 */
@Entity
@Table(name = "bid_increment_tiers")
@Getter
@Setter
@NoArgsConstructor
public class BidIncrementTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    @Column(name = "min_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal minAmount;

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "increment_type", length = 10, nullable = false)
    private String incrementType;

    @Column(name = "increment_value", precision = 15, scale = 2, nullable = false)
    private BigDecimal incrementValue;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
