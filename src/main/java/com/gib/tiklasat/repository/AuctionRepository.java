package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    // Satıcının aktif veya bitmiş açık artırmalarını bulmak için
    List<Auction> findByListingSellerIdAndStatusOrderByStartTimeDesc(UUID sellerId, String status);

    // 4. Sadece aktif (veya sadece bitmiş) olan açık artırmaları bulmak için
    List<Auction> findByStatus(String status);

    @Query(
        value = """
                WITH RECURSIVE category_tree AS (
                        SELECT id FROM categories WHERE id = :categoryId
                        UNION ALL
                        SELECT c.id FROM categories c
                        JOIN category_tree ct ON c.parent_id = ct.id
                )
                SELECT a.* FROM auctions a
                JOIN listings l ON a.listing_id = l.id
                WHERE a.status = 'ACTIVE' AND a.ends_at > now()
                AND (:search IS NULL OR l.title ILIKE CONCAT('%', :search, '%'))
                AND (:categoryId IS NULL OR l.category_id IN (SELECT id FROM category_tree))
                """,
        countQuery = """
                WITH RECURSIVE category_tree AS (
                        SELECT id FROM categories WHERE id = :categoryId
                        UNION ALL
                        SELECT c.id FROM categories c
                        JOIN category_tree ct ON c.parent_id = ct.id
                )
                SELECT count(*) FROM auctions a
                JOIN listings l ON a.listing_id = l.id
                WHERE a.status = 'ACTIVE' AND a.ends_at > now()
                AND (:search IS NULL OR l.title ILIKE CONCAT('%', :search, '%'))
                AND (:categoryId IS NULL OR l.category_id IN (SELECT id FROM category_tree))
                """,

        nativeQuery = true
        )
        Page<Auction> searchActiveAuctions(
        @Param("search") String search,
        @Param("categoryId") UUID categoryId,
        Pageable pageable
        );

    // 5. EN ÇOK FAVORİLENEN aktif açık artırmalar — "Most Wanted" bölümü için
    // categoryId verilirse sadece o kategoriyle sınırlar (Haftanın Kategorileri bölümü için)
    @Query(
        value = """
                SELECT a.* FROM auctions a
                JOIN listings l ON a.listing_id = l.id
                LEFT JOIN favorites f ON f.auction_id = a.id
                WHERE a.status = 'ACTIVE' AND a.ends_at > now()
                AND (:categoryId IS NULL OR l.category_id = :categoryId)
                GROUP BY a.id
                ORDER BY count(f.id) DESC, a.ends_at ASC
                LIMIT :limit
                """,
        nativeQuery = true
    )
    List<Auction> findMostFavorited(@Param("limit") int limit, @Param("categoryId") UUID categoryId);

    List<Auction> findByStatusAndEndingSoonNotifiedFalseAndEndTimeBetween(String status, Instant from, Instant to);

    // İlan sayısını statüye göre getirmek için
    long countByStatus(String status);
}
