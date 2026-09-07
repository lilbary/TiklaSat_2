package com.gib.tiklasat.event;

import com.gib.tiklasat.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationService notificationService;

    /**
     * Ana işlem (teklif, onay, kapanış...) COMMIT olduktan sonra çalışır.
     * Bu noktada ana transaction bağlantısını çoktan iade etmiştir, dolayısıyla
     * yazma sırasında iki bağlantının aynı anda tutulması durumu hiç oluşmaz.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationRequested(NotificationRequestedEvent event) {
        try {
            notificationService.writeNotifications(event.items());
        } catch (Exception e) {
            // Ana işlem zaten commit edildi; buradaki hata onu etkileyemez.
            log.error("Bildirimler yazılamadı ({} adet)", event.items().size(), e);
        }
    }
}