package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BidRepository extends JpaRepository<Bid, UUID> {
    
    // 1. Bir açık artırmaya verilmiş TÜM TEKLİFLERİ bul ve tarihe göre YENİDEN ESKİYE doğru sırala
    // PESSIMISTIC_WRITE: Aynı anda iki kişi teklif verirse, veritabanı kilitlenir ve sıraya konulurlar (Race condition çözümü).
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    List<Bid> findByAuctionIdOrderByCreatedAtDesc(UUID auctionId);
    
    // 1b. Aynı sorgu ama KİLİTSİZ — teklif geçmişini sayfada göstermek için.
    // (findByAuctionIdOrderByCreatedAtDesc kilitli olduğu için sadece placeBid() transaction'ı
    // içinde kullanılmalı, sayfa görüntüleme gibi salt okuma işlerinde bu metot kullanılır.)
    List<Bid> findAllByAuctionIdOrderByCreatedAtDesc(UUID auctionId);

    // 2. Bir kullanıcının (Ahmet'in) geçmişte verdiği tüm teklifleri bul
    List<Bid> findByBidderId(UUID bidderId);

    // 3. Bir açık artırmadaki EN YÜKSEK teklifi getir (Kazananı bulmak için)
    Optional<Bid> findTopByAuctionIdOrderByAmountDesc(UUID auctionId);
}
