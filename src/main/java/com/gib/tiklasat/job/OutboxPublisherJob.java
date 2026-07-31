package com.gib.tiklasat.job;

import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Gidecek Mesajlar (Outbox) kutusunu dinleyen bot.
 * Sistemin herhangi bir yerinde oluşan olayı (OutboxEvent) alıp
 * WebSocket üzerinden ilgili kişilere fırlatır.
 */
@Component
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Her 5 saniyede bir çalışır
    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishEvents() {
        // Her seferinde en fazla 100 mesaj oku
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByPublishedAtIsNullOrderByIdAsc(
            org.springframework.data.domain.PageRequest.of(0, 100)
        );

        for (OutboxEvent event : pendingEvents) {
            try {
                // Hangi kanala mesaj atılacak?
                // Örn: /topic/auctions/{auctionId} kanalını dinleyen herkese fırlat.
                String destination = "/topic/" + event.getAggregateType().toLowerCase() + "s/" + event.getAggregateId();
                
                // Mesajı fırlat (Payload zaten JSON formatında)
                messagingTemplate.convertAndSend(destination, event.getPayload());

                // Olay başarıyla fırlatıldıysa zaman damgasını bas
                event.setPublishedAt(Instant.now());
            } catch (Exception e) {
                // Hata alırsak deneme sayısını artır
                event.setAttemptCount((short) (event.getAttemptCount() + 1));
                event.setLastError(e.getMessage());
            }
            outboxEventRepository.save(event);
        }
    }
}
