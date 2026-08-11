package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Listing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    Page<Listing> findBySellerId(UUID sellerId, Pageable pageable);
    Page<Listing> findByCategoryIdAndStatus(UUID categoryId, String status, Pageable pageable);
    Page<Listing> findByStatus(String status, Pageable pageable);
}
