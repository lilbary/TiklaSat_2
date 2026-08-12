package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    
    // 2. Belirli bir ilana (Listing) bağlı açık artırmayı bul (İlan sayfasında göstermek için)
    Optional<Auction> findByListingId(UUID listingId);
    
    // 3. STATÜSÜ AKTİF OLAN VE BİTİŞ TARİHİ ŞU ANDAN DAHA ESKİ OLANLARI BUL (Süresi dolmuş ama kapanmamış olanlar)
    List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, Instant now);
    
    // 2. Sadece aktif (veya sadece bitmiş) olan açık artırmaları bulmak için
    List<Auction> findByStatus(String status);
}
