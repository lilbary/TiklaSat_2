package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * İlan Görseli (Listing Image) varlığı.
 * İlanlara ait fotoğrafların medya bilgilerini ve sıralamasını tutar.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listing_images")
public class ListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false, foreignKey = @ForeignKey(name = "fk_listing_images_listing_id"))
    private Listing listing;

    @Column(name = "storage_key", length = 255, nullable = false, unique = true)
    private String storageKey;

    @Column(name = "content_type", length = 50, nullable = false)
    private String contentType;

    @Column(name = "size_bytes", nullable = false)
    private Integer sizeBytes;

    @Column(nullable = false)
    private Integer width;

    @Column(nullable = false)
    private Integer height;

    @Column(name = "sort_order", nullable = false)
    private Short sortOrder;

    @Column(name = "is_cover", nullable = false)
    private Boolean isCover = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
