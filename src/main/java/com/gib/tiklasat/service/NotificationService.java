package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.NotificationDto;
import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.Notification;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.NotificationRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Diğer servislerin (örn. BidService) çağıracağı, bildirim OLUŞTURAN iç yardımcı metot
    @Transactional
    public void createNotification(User user, Auction auction, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setAuction(auction);
        notification.setMessage(message);
        notificationRepository.save(notification);
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
}