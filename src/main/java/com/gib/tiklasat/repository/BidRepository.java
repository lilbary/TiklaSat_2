package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    
    List<Bid> findByAuctionIdOrderByAmountDesc(UUID auctionId);
    
    List<Bid> findByBidderIdOrderByCreatedAtDesc(UUID bidderId);
    
    long countByAuctionId(UUID auctionId);
}
