package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.AuctionExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionExtensionRepository extends JpaRepository<AuctionExtension, UUID> {
    
    List<AuctionExtension> findByAuctionIdOrderByExtensionNo(UUID auctionId);
}
