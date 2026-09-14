package com.gib.tiklasat.notification;

import com.gib.tiklasat.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    
    List<Notification> findByUserIdAndReadFalse(UUID userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId);
}