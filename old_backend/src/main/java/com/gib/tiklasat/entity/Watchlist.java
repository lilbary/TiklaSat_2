package com.gib.tiklasat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Kullanıcıların takip ettikleri ilanları tutan varlık sınıfı.
 * İzleme listesine eklenen ilanlar için kullanılır.
 */
@Entity
@Table(name = "watchlist")
@IdClass(Watchlist.WatchlistId.class)
@Getter
@Setter
@NoArgsConstructor
public class Watchlist {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watchlist_user"))
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false, foreignKey = @ForeignKey(name = "fk_watchlist_listing"))
    private Listing listing;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @EqualsAndHashCode
    public static class WatchlistId implements Serializable {
        private UUID user;
        private UUID listing;
    }
}
