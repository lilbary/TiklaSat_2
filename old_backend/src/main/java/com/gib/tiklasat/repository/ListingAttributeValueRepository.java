package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.ListingAttributeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ListingAttributeValueRepository extends JpaRepository<ListingAttributeValue, UUID> {
    List<ListingAttributeValue> findByListingId(UUID listingId);
}
