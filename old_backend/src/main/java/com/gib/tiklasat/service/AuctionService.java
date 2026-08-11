package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.auction.AuctionCreateRequest;
import com.gib.tiklasat.dto.auction.AuctionDto;
import com.gib.tiklasat.dto.auction.BidRequest;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.AuctionExtension;
import com.gib.tiklasat.entity.Bid;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.AuctionExtensionRepository;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final AuctionExtensionRepository extensionRepository;

    private static final int SNIPER_WINDOW_SECONDS = 120; // Son 2 dakika
    private static final int EXTENSION_SECONDS = 120; // 2 dakika uzat
    private static final int MAX_EXTENSIONS = 20;

    /**
     * İlan için yeni bir açık artırma başlatır.
     */
    @Transactional
    public AuctionDto createAuction(AuctionCreateRequest request, String sellerEmail) {
        Listing listing = listingRepository.findById(request.getListingId())
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new IllegalStateException("Sadece kendi ilanınızı açık artırmaya çıkarabilirsiniz");
        }

        Auction auction = new Auction();
        auction.setListing(listing);
        auction.setStartPrice(request.getStartPrice());
        auction.setCurrentPrice(request.getStartPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setCurrency("TRY");
        auction.setBidCount(0);
        auction.setStartsAt(Instant.now());
        auction.setOriginalEndsAt(request.getEndsAt());
        auction.setEndsAt(request.getEndsAt());
        auction.setExtensionCount((short) 0);
        auction.setStatus("ACTIVE");

        auction = auctionRepository.save(auction);
        
        // İlan durumunu da güncelle
        listing.setStatus("APPROVED");
        listing.setPublishedAt(Instant.now());
        listingRepository.save(listing);

        return mapToDto(auction);
    }

    /**
     * Teklif Verme Motoru (PESSIMISTIC_WRITE Kilitli)
     * Veritabanı seviyesinde satırı kilitler. İki kişi aynı anda aynı müzayedeye
     * teklif vermeye çalışırsa, biri beklemek zorundadır.
     */
    @Transactional
    public void placeBid(BidRequest request, String bidderEmail, String ipAddress, String userAgent) {
        User bidder = userRepository.findByEmail(bidderEmail)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        // PESİMİSTİK KİLİT - Açık artırmayı getir ve satırı KİLİTLE!
        // Aynı anda sadece 1 thread bu kodu geçebilir.
        Auction auction = auctionRepository.findByIdForUpdate(request.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Açık artırma bulunamadı"));

        // Kurallar
        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new IllegalStateException("Bu açık artırma aktif değil (Durum: " + auction.getStatus() + ")");
        }
        
        if (Instant.now().isAfter(auction.getEndsAt())) {
            throw new IllegalStateException("Açık artırma süresi dolmuş!");
        }

        if (auction.getListing().getSeller().getId().equals(bidder.getId())) {
            throw new IllegalStateException("Kendi ürününüze teklif veremezsiniz!");
        }

        BigDecimal minRequiredBid = auction.getCurrentPrice();
        if (auction.getBidCount() > 0) {
            // Şimdilik sabit artış kuralı, ileride BidIncrementTier kullanılabilir.
            minRequiredBid = minRequiredBid.add(BigDecimal.valueOf(100)); // Min 100 TL artış.
        }

        if (request.getAmount().compareTo(minRequiredBid) < 0) {
            throw new IllegalArgumentException("Teklifiniz çok düşük! Minimum verilmesi gereken teklif: " + minRequiredBid);
        }

        // Teklifi oluştur
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(request.getAmount());
        bid.setMaxAmount(request.getMaxAmount()); // Opsiyonel proxy bid limit
        bid.setIsProxy(false);
        bid.setStatus("WINNING");
        bid.setIpAddress(ipAddress);
        bid.setUserAgent(userAgent);

        // Önceki teklifleri OUTBID (Geçildi) yap
        if (auction.getHighestBid() != null) {
            Bid previousHighest = auction.getHighestBid();
            previousHighest.setStatus("OUTBID");
            bidRepository.save(previousHighest);
        }

        bid = bidRepository.save(bid);

        // Açık artırmayı güncelle
        auction.setCurrentPrice(bid.getAmount());
        auction.setBidCount(auction.getBidCount() + 1);
        auction.setHighestBid(bid);

        // Anti-Sniper Koruması (Süre uzatma kontrolü)
        long secondsLeft = ChronoUnit.SECONDS.between(Instant.now(), auction.getEndsAt());
        if (secondsLeft <= SNIPER_WINDOW_SECONDS && auction.getExtensionCount() < MAX_EXTENSIONS) {
            Instant previousEndsAt = auction.getEndsAt();
            Instant newEndsAt = auction.getEndsAt().plusSeconds(EXTENSION_SECONDS);
            
            auction.setEndsAt(newEndsAt);
            auction.setExtensionCount((short) (auction.getExtensionCount() + 1));

            // Uzatma kaydı oluştur
            AuctionExtension extension = new AuctionExtension();
            extension.setAuction(auction);
            extension.setBid(bid);
            extension.setExtensionNo(auction.getExtensionCount());
            extension.setPreviousEndsAt(previousEndsAt);
            extension.setNewEndsAt(newEndsAt);
            extensionRepository.save(extension);
        }

        auctionRepository.save(auction);
    }

    public AuctionDto getAuction(UUID auctionId) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Açık artırma bulunamadı"));
        return mapToDto(auction);
    }

    private AuctionDto mapToDto(Auction auction) {
        boolean reserveMet = true;
        if (auction.getReservePrice() != null) {
            reserveMet = auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;
        }

        return AuctionDto.builder()
                .id(auction.getId())
                .listingId(auction.getListing().getId())
                .startPrice(auction.getStartPrice())
                .currentPrice(auction.getCurrentPrice())
                .bidCount(auction.getBidCount())
                .startsAt(auction.getStartsAt())
                .endsAt(auction.getEndsAt())
                .status(auction.getStatus())
                .reservePriceMet(reserveMet) // Gizli taban fiyata ulaşıldı mı bilgisi
                .build();
    }
}
