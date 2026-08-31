package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Notification;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class NotificationDto {
    private UUID id;
    private UUID auctionId;
    private String message;
    private boolean read;
    private Instant createdAt;

    public static NotificationDto fromEntity(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setAuctionId(notification.getAuction().getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}