package com.gib.tiklasat.job;

import com.gib.tiklasat.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuctionClosingJob {

    private final AuctionService auctionService;

    // Her 60 saniyede bir (60.000 milisaniye) otomatik olarak çalışır
    @Scheduled(fixedRate = 60000)
    public void closeAuctions() {
        System.out.println("[CRON JOB] " + Instant.now() + " -> Süresi biten açık artırmalar kontrol ediliyor...");
        auctionService.closeExpiredAuctions();
    }
}
