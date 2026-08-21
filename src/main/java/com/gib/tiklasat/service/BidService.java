package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.BidDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Bid;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ForbiddenActionException;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository; // Outbox Tablosu
    private final ObjectMapper objectMapper; // JSON Çevirici

    // TEKLİF VERME (PLACE BID) METODU
    @Transactional
    public BidDto placeBid(UUID auctionId, String bidderEmail, BigDecimal amount) {

        // 1. KURAL: Açık artırmayı KİLİTLEYEREK (Pessimistic Lock) getir
        Auction auction = auctionRepository.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Açık artırma bulunamadı!"));

        // 2. KURAL: Açık artırma hala "ACTIVE" mi ve süresi bitmemiş mi?
        if (!auction.getStatus().equals("ACTIVE") || auction.getEndTime().isBefore(Instant.now())) {
            throw new RuntimeException("Bu açık artırma bitmiş veya iptal edilmiş, teklif veremezsiniz!");
        }

        // 3. KURAL: Teklif veren kullanıcı sistemde var mı? (JWT'deki email'den bulunuyor)
        User bidder = userRepository.findByEmail(bidderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        // 4. KURAL (Kritik): Kendi ilanıma teklif verebilir miyim? HAYIR!
        if (auction.getListing().getSeller().getId().equals(bidder.getId())) {
            throw new ForbiddenActionException("Kendi açık artırmanıza teklif veremezsiniz, kurnazlık yapmayın!");
        }

        // 5 ve 6. KURAL: Kademeli Artış (Bid Increments) Kontrolü
        List<Bid> existingBids = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
        
        BigDecimal requiredMinimumBid;
        if (existingBids.isEmpty()) {
            // Açık artırmaya gelen İLK teklif ise, en az başlangıç fiyatı kadar olmalı
            requiredMinimumBid = auction.getStartingPrice();
        } else {
            // Zaten teklif varsa, mevcut en yüksek teklifin üzerine kademeli minimum artış eklenmeli
            BigDecimal currentHighest = existingBids.get(0).getAmount();
            BigDecimal minIncrement = calculateMinIncrement(currentHighest);
            requiredMinimumBid = currentHighest.add(minIncrement);
        }

        if (amount.compareTo(requiredMinimumBid) < 0) {
            throw new ConflictException("Teklifiniz çok düşük! Vermeniz gereken minimum tutar: " + requiredMinimumBid + " TL");
        }

        // TÜM KURALLARI GEÇTİYSE: Yeni teklifi oluştur ve kaydet
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);

        // saveAndFlush KASITLI: save() tek başına INSERT'i hemen çalıştırmaz (flush transaction
        // sonunda olur), bu yüzden @CreationTimestamp ile üretilen createdAt bu noktada henüz
        // Java nesnesine yansımamış olurdu (null kalırdı) — aşağıda DTO'ya koyup WebSocket'e
        // yollayacağımız için hemen flush ederek gerçek değeri garantiliyoruz.
        bid = bidRepository.saveAndFlush(bid);

        Instant now = Instant.now();
        Instant sniperWindow = auction.getEndTime().minus(Duration.ofSeconds(120));
        if (now.isAfter(sniperWindow) && auction.getExtensionCount() < 20) {
            Instant newEnd = now.plus(Duration.ofSeconds(120));
            auction.setEndTime(newEnd);
            auction.setExtensionCount(auction.getExtensionCount() + 1);
            auctionRepository.save(auction);
        }



        // Outbox Pattern: WebSocket'e hemen haber verme, Outbox (Giden Kutusu) tablosuna not bırak.
        BidDto result = BidDto.fromEntity(bid);
        
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventType("BID_PLACED");
            
            // Kuryenin (Publisher Job) mesajı nereye ve ne olarak ileteceğini JSON'a yazıyoruz
            String jsonPayload = objectMapper.writeValueAsString(
                java.util.Map.of(
                    "destination", "/topic/auctions." + auctionId,
                    "payload", result
                )
            );
            event.setPayload(jsonPayload);
            outboxEventRepository.save(event);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Outbox mesajı oluşturulamadı", e);
        }

        return result;
    }

    // BİR AÇIK ARTIRMANIN TEKLİF GEÇMİŞİNİ GETİR (Yeniden eskiye doğru)
    @Transactional(readOnly = true)
    public List<BidDto> getBidHistory(UUID auctionId) {
        return bidRepository.findAllByAuctionIdOrderByCreatedAtDesc(auctionId).stream()
                .map(BidDto::fromEntity)
                .toList();
    }

    // YARDIMCI METOT: Kademeli Artış Tablosu
    // Fiyat arttıkça, yapılması gereken minimum teklif artışı da büyür.
    private BigDecimal calculateMinIncrement(BigDecimal currentPrice) {
        if (currentPrice.compareTo(new BigDecimal("100")) < 0) return new BigDecimal("1");
        if (currentPrice.compareTo(new BigDecimal("500")) < 0) return new BigDecimal("5");
        if (currentPrice.compareTo(new BigDecimal("1000")) < 0) return new BigDecimal("10");
        if (currentPrice.compareTo(new BigDecimal("5000")) < 0) return new BigDecimal("50");
        if (currentPrice.compareTo(new BigDecimal("10000")) < 0) return new BigDecimal("100");
        if (currentPrice.compareTo(new BigDecimal("50000")) < 0) return new BigDecimal("500");
        if (currentPrice.compareTo(new BigDecimal("100000")) < 0) return new BigDecimal("1000");
        return new BigDecimal("5000");
    }
}
