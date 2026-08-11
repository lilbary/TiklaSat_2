package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, UUID> {
    List<ListingImage> findByListingIdOrderBySortOrder(UUID listingId);
    long countByListingId(UUID listingId);
}
