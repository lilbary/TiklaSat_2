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
 * Kullanıcı değerlendirmelerini tutan varlık sınıfı.
 * Müzayede sonrası alıcı ve satıcıların birbirlerini değerlendirmesi için kullanılır.
 */
@Entity
@Table(name = "user_ratings")
@Getter
@Setter
@NoArgsConstructor
public class UserRating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_ratings_auction"))
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_ratings_rater"))
    private User rater;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ratee_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_ratings_ratee"))
    private User ratee;

    @Column(name = "score", nullable = false)
    private Short score;

    @Column(name = "comment", length = 500)
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
