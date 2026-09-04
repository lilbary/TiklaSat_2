package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Favorite;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ForbiddenActionException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.FavoriteRepository;
import com.gib.tiklasat.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {

    private final NotificationService notificationService;
    private final AuctionRepository auctionRepository;
    private final ListingRepository listingRepository; // İlanın var olup olmadığını kontrol etmek için lazım
    private final BidRepository bidRepository;          // Kazananı bulmak için en yüksek teklifi sorgulayacağız
    private final FavoriteRepository favoriteRepository;

    // YENİ BİR AÇIK ARTIRMA BAŞLATMA METODU
    @Transactional
    @CacheEvict(value = {"auctions_all", "auction_by_id"}, allEntries = true)
    public AuctionDto createAuction(UUID listingId, BigDecimal startingPrice, Instant endTime, BigDecimal reservePrice, String sellerEmail) {

        Listing listing = listingRepository.findById(listingId)
            .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı!"));

        // YENİ KURAL: Bu ilan gerçekten bu isteği atan kişiye mi ait?
        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new ForbiddenActionException("Bu ilan size ait değil, artırmaya çıkaramazsınız!");
        }
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
        
        // KURAL: Bitiş tarihi 14 günü geçemez (BR-A-004)
        if (Duration.between(Instant.now(), endTime).toDays() > 14) {
            throw new RuntimeException("Açık artırma süresi en fazla 14 gün olabilir!");
        }

        if (reservePrice != null && reservePrice.compareTo(startingPrice) < 0) {
        throw new RuntimeException("Rezerv fiyat, başlangıç fiyatından düşük olamaz!");
        }

        // Tüm kuralları geçtiyse boş bir Açık Artırma (Entity) oluştur ve verileri doldur
        Auction auction = new Auction();
        auction.setListing(listing);
        auction.setStartingPrice(startingPrice);
        auction.setCurrentPrice(startingPrice);
        auction.setStartTime(Instant.now()); // Başlangıç zamanı ŞU AN.
        auction.setEndTime(endTime);
        auction.setStatus("PENDING");
        auction.setOriginalEndsAt(endTime);
        auction.setReservePrice(reservePrice);

        // Veritabanına kaydet
        auction = auctionRepository.save(auction);

        // Kullanıcıya Süzgeçten (DTO) geçirerek geri döndür — yeni açılan artırmada
        // henüz hiç teklif yok, güncel fiyat = başlangıç fiyatı.
        return AuctionDto.fromEntity(auction, auction.getStartingPrice());
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
            // KAZANANI BELİRLE: Bu ihaleye verilmiş en yüksek teklifi bul
            bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId())
                    .ifPresentOrElse(
                            topBid -> {
                                boolean reserveMet = auction.getReservePrice() == null
                                        || topBid.getAmount().compareTo(auction.getReservePrice()) >= 0;

                                if (reserveMet) {
                                    // Teklif rezervi karşılıyor (ya da rezerv hiç yok) -> satış gerçekleşir
                                    auction.setStatus("ENDED");
                                    auction.setWinner(topBid.getBidder());
                                    log.info("KAZANAN - Açık Artırma {} kazananı: {} (Tutar: {} TL)",
                                            auction.getId(),
                                            topBid.getBidder().getFullName(),
                                            topBid.getAmount());

                                    String winnerMessage = "'" + auction.getListing().getTitle() + "' açık artırmasını kazandınız! Kazanan teklif: " + topBid.getAmount() + " TL";
                                    try {
                                        notificationService.createNotification(topBid.getBidder(), auction, winnerMessage);
                                    } catch (Exception e) {
                                        log.error("Bildirim oluşturulamadı, açık artırma kapanışı devam ediyor", e);
                                    }

                                    String sellerMessage = "'" + auction.getListing().getTitle() + "' ilanınız " + topBid.getAmount() + " TL'ye satıldı!";
                                    try {
                                        notificationService.createNotification(auction.getListing().getSeller(), auction, sellerMessage);
                                    } catch (Exception e) {
                                        log.error("Bildirim oluşturulamadı, açık artırma kapanışı devam ediyor", e);
                                    }
                                } else {
                                    // En yüksek teklif rezervin ALTINDA -> satış gerçekleşmez
                                    auction.setStatus("RESERVE_NOT_MET");
                                    log.info("REZERV KARŞILANMADI - Açık Artırma {} (En yüksek teklif: {} TL, Rezerv: {} TL)",
                                            auction.getId(),
                                            topBid.getAmount(),
                                            auction.getReservePrice());

                                    String bidderMessage = "'" + auction.getListing().getTitle() + "' açık artırmasında en yüksek teklif sizindi, ama satıcının belirlediği minimum fiyata ulaşılamadığı için satış gerçekleşmedi.";
                                    try {
                                        notificationService.createNotification(topBid.getBidder(), auction, bidderMessage);
                                    } catch (Exception e) {
                                        log.error("Bildirim oluşturulamadı, açık artırma kapanışı devam ediyor", e);
                                    }

                                    String sellerMessage = "'" + auction.getListing().getTitle() + "' ilanınız için en yüksek teklif, belirlediğiniz rezerv fiyatın altında kaldı. Satış gerçekleşmedi.";
                                    try {
                                        notificationService.createNotification(auction.getListing().getSeller(), auction, sellerMessage);
                                    } catch (Exception e) {
                                        log.error("Bildirim oluşturulamadı, açık artırma kapanışı devam ediyor", e);
                                    }
                                }
                            },
                            () -> {
                                // Hiç teklif verilmemişse -> kazanan yok, rezerv zaten konu değil
                                auction.setStatus("ENDED");
                                log.info("UYARI - Açık Artırma {} teklif almadan kapandı.",
                                        auction.getId());
                            }
                    );

            auctionRepository.save(auction);
        }
    }

    // Arama/kategori filtresiyle, sayfalı açık artırma listesi — filtreleme artık
    // veritabanında (native SQL sorgusuyla) yapılıyor, cache YOK çünkü arama
    // terimi kombinasyonu sonsuz, cache'lemek anlamsız/verimsiz olurdu.
    @Transactional(readOnly = true)
    public Page<AuctionDto> searchAuctions(String search, UUID categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Auction> auctionPage = auctionRepository.searchActiveAuctions(search, categoryId, pageable);
        return auctionPage.map(a -> AuctionDto.fromEntity(a, a.getCurrentPrice()));
    }

    // TÜM AÇIK ARTIRMALARI LİSTELE (Ana Sayfa İçin)
    @Transactional(readOnly = true)
    @Cacheable(value = "auctions_all")
    public List<AuctionDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(a -> AuctionDto.fromEntity(a, a.getCurrentPrice()))
                .collect(Collectors.toList());
    }

    // EN ÇOK FAVORİLENEN AÇIK ARTIRMALAR — "Most Wanted" bölümü için
    @Transactional(readOnly = true)
    public List<AuctionDto> getMostFavorited(int limit, UUID categoryId) {
        return auctionRepository.findMostFavorited(limit, categoryId).stream()
                .map(a -> AuctionDto.fromEntity(a, a.getCurrentPrice()))
                .collect(Collectors.toList());
    }

    // TEK BİR AÇIK ARTIRMANIN DETAYINI GETİR
    @Transactional(readOnly = true)
    @Cacheable(value = "auction_by_id", key = "#id")
    public AuctionDto getAuctionById(UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Açık artırma bulunamadı!"));
        return AuctionDto.fromEntity(auction, auction.getCurrentPrice());
    }

    @Transactional(readOnly = true)
    public List<AuctionDto> getAuctionsBySeller(UUID sellerId, String status) {
        return auctionRepository.findByListingSellerIdAndStatusOrderByStartTimeDesc(sellerId, status)
                .stream()
                .map(a -> AuctionDto.fromEntity(a, a.getCurrentPrice()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // Her 1 dakikada bir — hassasiyet gerekmiyor
    @net.javacrumbs.shedlock.spring.annotation.SchedulerLock(name = "notifyEndingSoonTask", lockAtLeastFor = "PT5S", lockAtMostFor = "PT50S")
    public void notifyFavoritedAuctionsEndingSoon() {
        Instant now = Instant.now();
        Instant in1Hour = now.plus(Duration.ofHours(1));

        List<Auction> endingSoon = auctionRepository
                .findByStatusAndEndingSoonNotifiedFalseAndEndTimeBetween("ACTIVE", now, in1Hour);

        for (Auction auction : endingSoon) {
            List<Favorite> favorites = favoriteRepository.findByAuctionId(auction.getId());
            String message = "Favorilediğin '" + auction.getListing().getTitle() + "' açık artırmasının süresi 1 saatten az kaldı!";
            for (Favorite favorite : favorites) {
                try {
                    notificationService.createNotification(favorite.getUser(), auction, message);
                } catch (Exception e) {
                    log.error("Bildirim oluşturulamadı, bitmek üzere kontrolü devam ediyor", e);
                }
            }
            auction.setEndingSoonNotified(true);
            auctionRepository.save(auction);
        }
    }
}
