package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ListingRepository listingRepository; // İlanın var olup olmadığını kontrol etmek için lazım
    private final BidRepository bidRepository;          // Kazananı bulmak için en yüksek teklifi sorgulayacağız

    // YENİ BİR AÇIK ARTIRMA BAŞLATMA METODU
    @Transactional
    @CacheEvict(value = {"auctions_all", "auction_by_id"}, allEntries = true)
    public AuctionDto createAuction(UUID listingId, BigDecimal startingPrice, Instant endTime) {
        
        // KURAL 1: Böyle bir ilan gerçekten var mı? Yoksa hata ver.
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı!"));

        // KURAL 2: Bu ilanın zaten devam eden bir açık artırması var mı?
        if (auctionRepository.findByListingId(listingId).isPresent()) {
            throw new ConflictException("Bu ilan için zaten bir açık artırma oluşturulmuş!");
        }

        // KURAL 3: Başlangıç fiyatı 0'dan büyük olmalı
        if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Başlangıç fiyatı sıfırdan büyük olmalıdır!");
        }

        // KURAL 4: Bitiş tarihi şu andan daha ileride bir zaman olmalı
        if (endTime.isBefore(Instant.now())) {
            throw new RuntimeException("Bitiş tarihi geçmiş bir zaman olamaz!");
        }

        // Tüm kuralları geçtiyse boş bir Açık Artırma (Entity) oluştur ve verileri doldur
        Auction auction = new Auction();
        auction.setListing(listing);
        auction.setStartingPrice(startingPrice);
        auction.setStartTime(Instant.now()); // Başlangıç zamanı ŞU AN.
        auction.setEndTime(endTime);
        auction.setStatus("ACTIVE");

        // Veritabanına kaydet
        auction = auctionRepository.save(auction);

        // Kullanıcıya Süzgeçten (DTO) geçirerek geri döndür — yeni açılan artırmada
        // henüz hiç teklif yok, güncel fiyat = başlangıç fiyatı.
        return AuctionDto.fromEntity(auction, auction.getStartingPrice());
    }

    // Bir artırmanın GERÇEK güncel fiyatını bulur: en yüksek teklif varsa o,
    // yoksa (hiç teklif gelmemişse) başlangıç fiyatı.
    private BigDecimal resolveCurrentPrice(Auction auction) {
        return bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId())
                .map(bid -> bid.getAmount())
                .orElse(auction.getStartingPrice());
    }

    // 3. AÇIK ARTIRMANIN BİTİŞ ZAMANI GELDİYSE DURUMUNU GÜNCELLE + KAZANANI BELİRLE
    @Transactional
    @Scheduled(fixedRate = 10000) // Her 10 saniyede bir çalışır
    @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "closeExpiredAuctionsTask", lockAtLeastFor = "PT2S", lockAtMostFor = "PT9S")
    @CacheEvict(value = {"auctions_all", "auction_by_id"}, allEntries = true)
    public void closeExpiredAuctions() {
        // Şu anki zamanı al
        Instant now = Instant.now();
        
        // Veritabanından "Aktif" ama bitiş tarihi "Şu an"dan daha eski olanları bul (Süresi dolmuşlar)
        List<Auction> expiredAuctions = auctionRepository.findByStatusAndEndTimeBefore("ACTIVE", now);
        
        for (Auction auction : expiredAuctions) {
            auction.setStatus("ENDED"); // Durumunu "Bitti" olarak işaretle

            // KAZANANI BELİRLE: Bu ihaleye verilmiş en yüksek teklifi bul
            bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId())
                    .ifPresentOrElse(
                            topBid -> {
                                // Teklif varsa -> kazanan = en yüksek teklifi veren kişi
                                auction.setWinner(topBid.getBidder());
                                log.info("KAZANAN - Açık Artırma {} kazananı: {} (Tutar: {} TL)",
                                        auction.getId(),
                                        topBid.getBidder().getFullName(),
                                        topBid.getAmount());
                            },
                            () -> {
                                // Hiç teklif verilmemişse -> kazanan yok
                                log.info("UYARI - Açık Artırma {} teklif almadan kapandı.",
                                        auction.getId());
                            }
                    );

            auctionRepository.save(auction);
        }
    }

    // TÜM AÇIK ARTIRMALARI LİSTELE (Ana Sayfa İçin)
    @Transactional(readOnly = true)
    @Cacheable(value = "auctions_all")
    public List<AuctionDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(a -> AuctionDto.fromEntity(a, resolveCurrentPrice(a)))
                .collect(Collectors.toList());
    }

    // TEK BİR AÇIK ARTIRMANIN DETAYINI GETİR
    @Transactional(readOnly = true)
    @Cacheable(value = "auction_by_id", key = "#id")
    public AuctionDto getAuctionById(UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Açık artırma bulunamadı!"));
        return AuctionDto.fromEntity(auction, resolveCurrentPrice(auction));
    }
}
