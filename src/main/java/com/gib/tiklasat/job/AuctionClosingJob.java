package com.gib.tiklasat.job;

import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Süresi dolmuş müzayedeleri kapatan "Zamanlayıcı Bot".
 * Her dakikanın 0. saniyesinde çalışır (cron = "0 * * * * *").
 */
@Component
@RequiredArgsConstructor
public class AuctionClosingJob {

    private final AuctionRepository auctionRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void closeExpiredAuctions() {
        // Durumu ACTIVE olan ve bitiş süresi şu anki zamandan önce olanları bul
        List<Auction> expiredAuctions = auctionRepository.findByStatusAndEndsAtBefore("ACTIVE", Instant.now());

        for (Auction auction : expiredAuctions) {
            closeAuction(auction);
        }
    }

    private void closeAuction(Auction auction) {
        if (auction.getBidCount() == 0) {
            // Hiç teklif gelmemiş
            auction.setStatus("ENDED_NO_BIDS");
        } else {
            // Teklif gelmiş, taban fiyata ulaşıldı mı kontrol et
            boolean reserveMet = true;
            if (auction.getReservePrice() != null) {
                reserveMet = auction.getCurrentPrice().compareTo(auction.getReservePrice()) >= 0;
            }

            if (reserveMet) {
                // Başarıyla satıldı
                auction.setStatus("ENDED_SOLD");
                auction.setWinnerUser(auction.getHighestBid().getBidder());
            } else {
                // Taban fiyata ulaşılamadı
                auction.setStatus("ENDED_RESERVE_NOT_MET");
            }
        }

        auction.setClosedAt(Instant.now());
        auctionRepository.save(auction);
        
        // Not: Burada OutboxEvent tablosuna "Açık Artırma Bitti" olayı da yazılabilir.
        // Bu sayede kazanan kişiye bildirim gidebilir.
    }
}
