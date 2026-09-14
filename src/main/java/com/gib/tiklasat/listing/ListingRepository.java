package com.gib.tiklasat.listing;

import com.gib.tiklasat.listing.Listing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    List<Listing> findByCategoryId(UUID categoryId);
    List<Listing> findBySellerId(UUID sellerId);
    boolean existsByCategoryIdAndStatus(UUID categoryId, String status);

}
