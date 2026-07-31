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
 * Kullanıcı bildirimlerini tutan varlık sınıfı.
 * Teklif durumları ve ilan güncellemeleri gibi kullanıcıya gösterilecek bildirimleri yönetir.
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_notifications_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", foreignKey = @ForeignKey(name = "fk_notifications_listing"))
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", foreignKey = @ForeignKey(name = "fk_notifications_auction"))
    private Auction auction;

    @Column(name = "type", length = 32, nullable = false)
    private String type;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "body", length = 500, nullable = false)
    private String body;

    @Column(name = "payload", columnDefinition = "JSONB")
    private String payload;

    @Column(name = "read_at")
    private Instant readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
