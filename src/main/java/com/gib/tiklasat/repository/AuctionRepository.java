package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, UUID> {
    
    // 1. Pesimistik Kilit (Pessimistic Write Lock) ile açık artırmayı getir (Teklif Motoru İçin)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") UUID id);

    // 2. Belirli bir ilana (Listing) bağlı açık artırmayı bul (İlan sayfasında göstermek için)
    Optional<Auction> findByListingId(UUID listingId);
    
    // 3. STATÜSÜ AKTİF OLAN VE BİTİŞ TARİHİ ŞU ANDAN DAHA ESKİ OLANLARI BUL (Süresi dolmuş ama kapanmamış olanlar)
    List<Auction> findByStatusAndEndTimeBefore(String status, Instant now);
    
    // 4. Sadece aktif (veya sadece bitmiş) olan açık artırmaları bulmak için
    List<Auction> findByStatus(String status);
}
