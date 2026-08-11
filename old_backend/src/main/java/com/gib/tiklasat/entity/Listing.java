package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * İlan (Listing) varlığı.
 * Platformdaki ilanların ana bilgilerini tutar.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false, foreignKey = @ForeignKey(name = "fk_listings_seller_id"))
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_listings_category_id"))
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false, foreignKey = @ForeignKey(name = "fk_listings_city_id"))
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false, foreignKey = @ForeignKey(name = "fk_listings_district_id"))
    private District district;

    @Column(length = 70, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String description;

    @Column(length = 20, nullable = false)
    private String status = "DRAFT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by", foreignKey = @ForeignKey(name = "fk_listings_moderated_by"))
    private User moderatedBy;

    @Column(name = "moderated_at")
    private Instant moderatedAt;

    @Column(name = "moderation_note", length = 500)
    private String moderationNote;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;
}
