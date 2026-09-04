package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.dto.BidDto;
import com.gib.tiklasat.dto.MyBidDto;
import com.gib.tiklasat.dto.ReceivedBidDto;
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
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository; // Outbox Tablosu
    private final ObjectMapper objectMapper; // JSON Çevirici
    private final NotificationService notificationService;

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

        auction.setCurrentPrice(amount);

        // Önceki en yüksek teklifi veren kişiye "geçildin" bildirimi gönder
        if (!existingBids.isEmpty() && !existingBids.get(0).getBidder().getId().equals(bidder.getId())) {
            User outbidUser = existingBids.get(0).getBidder();
            String message = "'" + auction.getListing().getTitle() + "' için teklifiniz geçildi! Yeni fiyat: " + amount + " TL";
            try {
                notificationService.createNotification(outbidUser, auction, message);
            } catch (Exception e) {
                // Bildirim hatası teklifi iptal etmesin (BR-N-007)
                log.error("Bildirim oluşturulamadı, teklif işlemi devam ediyor", e);
            }
        }

        Instant now = Instant.now();
        Instant sniperWindow = auction.getEndTime().minus(Duration.ofSeconds(120));
        if (now.isAfter(sniperWindow) && auction.getExtensionCount() < 20) {
            Instant newEnd = now.plus(Duration.ofSeconds(120));
            auction.setEndTime(newEnd);
            auction.setExtensionCount(auction.getExtensionCount() + 1);
            auctionRepository.save(auction);

            // Önceden teklif vermiş herkese (şu anki teklifi verenin kendisi hariç) haber ver
            Set<User> previousBidders = existingBids.stream()
                    .map(Bid::getBidder)
                    .filter(u -> !u.getId().equals(bidder.getId()))
                    .collect(Collectors.toSet());

            String extensionMessage = "'" + auction.getListing().getTitle() + "' açık artırmasının süresi son dakika teklifiyle uzadı!";
            for (User previousBidder : previousBidders) {
                try {
                    notificationService.createNotification(previousBidder, auction, extensionMessage);
                } catch (Exception e) {
                    // Bildirim hatası uzatmayı iptal etmesin (BR-N-007)
                    log.error("Bildirim oluşturulamadı, süre uzatma işlemi devam ediyor", e);
                }
            }
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

    // KULLANICININ TEKLİF VERDİĞİ TÜM AÇIK ARTIRMALAR — her auction için EN YÜKSEK teklifim
    @Transactional(readOnly = true)
    public List<MyBidDto> getMyBids(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        List<Bid> myBids = bidRepository.findByBidderId(user.getId());

        // Aynı auction'a birden fazla teklif vermiş olabilirim — auction ID'sine göre grupla
        Map<UUID, List<Bid>> bidsByAuction = myBids.stream()
                .collect(Collectors.groupingBy(bid -> bid.getAuction().getId()));

        return bidsByAuction.values().stream()
                .map(bidsForOneAuction -> {
                    Bid highestBid = bidsForOneAuction.stream()
                            .max(Comparator.comparing(Bid::getAmount))
                            .orElseThrow();
                    Auction auction = highestBid.getAuction();

                    MyBidDto dto = new MyBidDto();
                    dto.setAuction(AuctionDto.fromEntity(auction, auction.getCurrentPrice()));
                    dto.setMyBidAmount(highestBid.getAmount());
                    dto.setWinning(highestBid.getAmount().compareTo(auction.getCurrentPrice()) == 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // İLANLARIMA VERİLMİŞ TÜM TEKLİFLER — her auction için EN YÜKSEK teklif + kim verdi
    @Transactional(readOnly = true)
    public List<ReceivedBidDto> getReceivedBids(String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        List<Bid> receivedBids = bidRepository.findByAuctionListingSellerId(seller.getId());

        Map<UUID, List<Bid>> bidsByAuction = receivedBids.stream()
                .collect(Collectors.groupingBy(bid -> bid.getAuction().getId()));

        return bidsByAuction.values().stream()
                .map(bidsForOneAuction -> {
                    Bid topBid = bidsForOneAuction.stream()
                            .max(Comparator.comparing(Bid::getAmount))
                            .orElseThrow();
                    Auction auction = topBid.getAuction();

                    ReceivedBidDto dto = new ReceivedBidDto();
                    dto.setAuction(AuctionDto.fromEntity(auction, auction.getCurrentPrice()));
                    dto.setTopBidAmount(topBid.getAmount());
                    dto.setTopBidderName(topBid.getBidder().getFullName());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // YARDIMCI METOT: Kademeli Artış Tablosu
    // Fiyat arttıkça, yapılması gereken minimum teklif artışı da büyür.
    private BigDecimal calculateMinIncrement(BigDecimal currentPrice) {
        if (currentPrice.compareTo(new BigDecimal("1000")) < 0) return new BigDecimal("25");
        if (currentPrice.compareTo(new BigDecimal("10000")) < 0) return new BigDecimal("100");
        if (currentPrice.compareTo(new BigDecimal("100000")) < 0) return new BigDecimal("500");
        if (currentPrice.compareTo(new BigDecimal("500000")) < 0) return new BigDecimal("2500");
        return currentPrice.multiply(new BigDecimal("0.01")); // %1 yüzdelik dilim
    }
}
