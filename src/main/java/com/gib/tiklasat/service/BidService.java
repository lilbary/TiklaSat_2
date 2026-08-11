package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.BidDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Bid;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    // TEKLİF VERME (PLACE BID) METODU
    @Transactional
    public BidDto placeBid(UUID auctionId, UUID bidderId, BigDecimal amount) {
        
        // 1. KURAL: Açık artırma veritabanında var mı?
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Açık artırma bulunamadı!"));

        // 2. KURAL: Açık artırma hala "ACTIVE" mi ve süresi bitmemiş mi?
        if (!auction.getStatus().equals("ACTIVE") || auction.getEndTime().isBefore(Instant.now())) {
            throw new RuntimeException("Bu açık artırma bitmiş veya iptal edilmiş, teklif veremezsiniz!");
        }

        // 3. KURAL: Teklif veren kullanıcı sistemde var mı?
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        // 4. KURAL (Kritik): Kendi ilanıma teklif verebilir miyim? HAYIR!
        if (auction.getListing().getSeller().getId().equals(bidderId)) {
            throw new RuntimeException("Kendi açık artırmanıza teklif veremezsiniz, kurnazlık yapmayın!");
        }

        // 5. KURAL: Verilen teklif, başlangıç fiyatından düşük olamaz!
        if (amount.compareTo(auction.getStartingPrice()) < 0) {
            throw new RuntimeException("Teklifiniz başlangıç fiyatından düşük olamaz!");
        }

        // 6. KURAL: Daha önce verilmiş teklifler varsa, en yüksek tekliften daha yüksek olmalı!
        List<Bid> existingBids = bidRepository.findByAuctionIdOrderByCreatedAtDesc(auctionId);
        if (!existingBids.isEmpty()) {
            BigDecimal highestBid = existingBids.get(0).getAmount();
            if (amount.compareTo(highestBid) <= 0) {
                throw new RuntimeException("Teklifiniz mevcut en yüksek tekliften (" + highestBid + ") daha yüksek olmalıdır!");
            }
        }

        // TÜM KURALLARI GEÇTİYSE: Yeni teklifi oluştur ve kaydet
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setBidder(bidder);
        bid.setAmount(amount);

        bid = bidRepository.save(bid);

        return BidDto.fromEntity(bid);
    }
}
