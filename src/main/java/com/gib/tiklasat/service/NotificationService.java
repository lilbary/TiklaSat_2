package com.gib.tiklasat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.entity.OutboxEvent;
import com.gib.tiklasat.repository.OutboxEventRepository;
import com.gib.tiklasat.dto.NotificationDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Notification;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.NotificationRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

    // Diğer servislerin (örn. BidService) çağıracağı, bildirim OLUŞTURAN iç yardımcı metot
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createNotification(User user, Auction auction, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setAuction(auction);
        notification.setMessage(message);
        notification = notificationRepository.saveAndFlush(notification);

        try {
            NotificationDto dto = NotificationDto.fromEntity(notification);
            OutboxEvent event = new OutboxEvent();
            event.setEventType("NOTIFICATION_CREATED");
            String jsonPayload = objectMapper.writeValueAsString(
                    Map.of(
                            "destination", "/topic/notifications." + user.getId(),
                            "payload", dto
                    )
            );
            event.setPayload(jsonPayload);
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Bildirim outbox mesajı oluşturulamadı", e);
        }
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