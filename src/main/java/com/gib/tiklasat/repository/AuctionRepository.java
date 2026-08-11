package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    
    // 1. İlan ID'sine göre o ilanın açık artırmasını bulmak için
    Auction findByListingId(UUID listingId);
    
    // 2. Sadece aktif (veya sadece bitmiş) olan açık artırmaları bulmak için
    List<Auction> findByStatus(String status);
}
