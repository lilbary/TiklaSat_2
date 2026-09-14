package com.gib.tiklasat.listing;

import com.gib.tiklasat.listing.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {
    // Bir ilana ait tüm görselleri getir
    List<ListingImage> findByListingId(UUID listingId);
}