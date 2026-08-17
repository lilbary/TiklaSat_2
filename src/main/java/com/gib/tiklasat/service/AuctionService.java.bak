package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final ListingRepository listingRepository; // İlanın var olup olmadığını kontrol etmek için lazım

    // YENİ BİR AÇIK ARTIRMA BAŞLATMA METODU
    @Transactional
    public AuctionDto createAuction(UUID listingId, BigDecimal startingPrice, Instant endTime) {
        
        // KURAL 1: Böyle bir ilan gerçekten var mı? Yoksa hata ver.
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı!"));

        // KURAL 2: Bu ilanın zaten devam eden bir açık artırması var mı?
        if (auctionRepository.findByListingId(listingId).isPresent()) {
            throw new RuntimeException("Bu ilan için zaten bir açık artırma oluşturulmuş!");
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

        // Kullanıcıya Süzgeçten (DTO) geçirerek geri döndür
        return AuctionDto.fromEntity(auction);
    }

    // 3. AÇIK ARTIRMANIN BİTİŞ ZAMANI GELDİYSE DURUMUNU GÜNCELLE
    @Transactional
    public void closeExpiredAuctions() {
        // Şu anki zamanı al
        java.time.Instant now = java.time.Instant.now();
        
        // Veritabanından "Aktif" ama bitiş tarihi "Şu an"dan daha eski olanları bul (Süresi dolmuşlar)
        java.util.List<com.gib.tiklasat.entity.Auction> expiredAuctions = auctionRepository.findByStatusAndEndTimeBefore("ACTIVE", now);
        
        for (com.gib.tiklasat.entity.Auction auction : expiredAuctions) {
            auction.setStatus("ENDED"); // Durumunu "Bitti" olarak işaretle
            auctionRepository.save(auction);
            
            System.out.println("ZAMAN DOLDU: Açık Artırma Kapatıldı -> ID: " + auction.getId());
            // (İleride buraya Kazananı belirleme ve bildirim atma kodları eklenebilir)
        }
    }

    // TÜM AÇIK ARTIRMALARI LİSTELE (Ana Sayfa İçin)
    @Transactional(readOnly = true)
    public java.util.List<AuctionDto> getAllAuctions() {
        return auctionRepository.findAll().stream()
                .map(AuctionDto::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    // TEK BİR AÇIK ARTIRMANIN DETAYINI GETİR
    @Transactional(readOnly = true)
    public AuctionDto getAuctionById(UUID id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Açık artırma bulunamadı!"));
        return AuctionDto.fromEntity(auction);
    }
}
