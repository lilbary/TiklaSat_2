package com.gib.tiklasat.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherJob {

    private final OutboxEventRepository outboxEventRepository;
    private final SimpMessagingTemplate messagingTemplate; // RabbitMQ'ya mesaj iletecek
    private final ObjectMapper objectMapper;

    // Her saniye (1000 ms) çalışır.
    // ShedLock sayesinde eğer arkada 3 sunucu çalışıyorsa, sadece 1 tanesi "Postacı" görevini üstlenir. Çakışma olmaz.
    @Scheduled(fixedRate = 1000)
    @SchedulerLock(name = "outboxPublisherTask", lockAtLeastFor = "500ms", lockAtMostFor = "2s")
    @Transactional
    public void publishEvents() {
        // 1. Panodan (Veritabanından) işlenmemiş notları al
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return; // Not yoksa geri dön
        }

        // 2. Her bir notu sırayla oku
        for (OutboxEvent event : events) {
            try {
                // Notu (JSON payload) çöz
                Map<String, Object> payloadMap = objectMapper.readValue(event.getPayload(), new TypeReference<>() {});
                String destination = (String) payloadMap.get("destination");
                Object messagePayload = payloadMap.get("payload");

                // 3. RabbitMQ'ya (Kuryeye) ilet
                messagingTemplate.convertAndSend(destination, messagePayload);

                // 4. İletildiğine göre notu "İşlendi" olarak işaretle (Çöpe at veya sakla)
                event.setProcessed(true);
            } catch (Exception e) {
                log.error("Outbox event işlenirken hata oluştu! Event ID: {}", event.getId(), e);
            }
        }
        
        // 5. Değişiklikleri veritabanına kaydet (Panoyu güncelle)
        outboxEventRepository.saveAll(events);
    }
}
