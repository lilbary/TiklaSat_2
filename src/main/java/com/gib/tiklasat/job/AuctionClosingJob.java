package com.gib.tiklasat.job;

import com.gib.tiklasat.service.AuctionService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class AuctionClosingJob {

    private final AuctionService auctionService;

    // Her 60 saniyede bir (60.000 milisaniye) otomatik olarak çalışır
    @Scheduled(fixedRate = 60000)
    @SchedulerLock(
            name = "closeExpiredAuctions",   // Kilidin veritabanındaki adı
            lockAtLeastFor = "30s",          // En az 30 sn kilitli kalsın (çok hızlı biterse bile tekrar çalışmasın)
            lockAtMostFor = "5m"             // En fazla 5 dk kilitli kalsın (sunucu çökerse diye güvenlik)
    )
    public void closeAuctions() {
        System.out.println("[CRON JOB] " + Instant.now() + " -> Süresi biten açık artırmalar kontrol ediliyor...");
        auctionService.closeExpiredAuctions();
    }
}
