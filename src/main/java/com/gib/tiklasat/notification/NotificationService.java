package com.gib.tiklasat.notification;

import com.gib.tiklasat.bid.BidService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.outbox.OutboxEvent;
import com.gib.tiklasat.outbox.OutboxEventRepository;
import com.gib.tiklasat.notification.NotificationDto;
import com.gib.tiklasat.auction.Auction;
import com.gib.tiklasat.notification.Notification;
import com.gib.tiklasat.user.User;
import com.gib.tiklasat.common.ResourceNotFoundException;
import com.gib.tiklasat.auction.AuctionRepository;
import com.gib.tiklasat.notification.NotificationRepository;
import com.gib.tiklasat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.gib.tiklasat.notification.NotificationRequestedEvent;
import org.springframework.context.ApplicationEventPublisher;
import java.util.ArrayList;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuctionRepository auctionRepository;

    // Diğer servislerin (örn. BidService) çağıracağı, bildirim OLUŞTURAN iç yardımcı metot
    // Tek bildirim. Çağıranların imzası hiç değişmiyor — 7 çağrı yerine dokunmuyoruz.
    public void createNotification(User user, Auction auction, String message) {
        createNotifications(List.of(
                new NotificationRequestedEvent.Item(user.getId(), auction.getId(), message)
        ));
    }

    // Toplu bildirim. Döngüde bildirim üreten yerler bunu kullanacak.
    public void createNotifications(List<NotificationRequestedEvent.Item> items) {
        if (items.isEmpty()) return;
        eventPublisher.publishEvent(new NotificationRequestedEvent(items));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeNotifications(List<NotificationRequestedEvent.Item> items) {

    // getReferenceById: veritabanına GİTMEDEN sadece kimliği taşıyan bir proxy döndürür.
    // Bize zaten sadece foreign key'i yazmak lazım, kullanıcının adı soyadı değil.
    // findById kullansaydık 80 bildirim için 80 ekstra SELECT atılırdı.
    List<Notification> notifications = items.stream()
            .map(item -> {
                Notification notification = new Notification();
                notification.setUser(userRepository.getReferenceById(item.userId()));
                notification.setAuction(auctionRepository.getReferenceById(item.auctionId()));
                notification.setMessage(item.message());
                return notification;
            })
            .toList();

    notificationRepository.saveAllAndFlush(notifications);

    List<OutboxEvent> outboxEvents = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationDto dto = NotificationDto.fromEntity(notification);
            OutboxEvent outboxEvent = new OutboxEvent();
            outboxEvent.setEventType("NOTIFICATION_CREATED");
            try {
                outboxEvent.setPayload(objectMapper.writeValueAsString(
                        Map.of(
                                "destination", "/topic/notifications." + notification.getUser().getId(),
                                "payload", dto
                        )
                ));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Bildirim outbox mesajı oluşturulamadı", e);
            }
            outboxEvents.add(outboxEvent);
        }
            outboxEventRepository.saveAll(outboxEvents);
    }   

    // Kullanıcının kendi bildirimlerini listelemesi
    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAllAsRead(String userEmail){
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));
                
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
    }
}