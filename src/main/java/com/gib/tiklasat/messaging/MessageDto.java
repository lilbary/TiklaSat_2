package com.gib.tiklasat.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Tek bir mesaj. Gönderenin adı da taşınıyor ki arayüz balonu çizerken
 * ayrıca kullanıcı sorgusu atmasın.
 */
public record MessageDto(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String senderName,
        String body,
        Instant createdAt,
        Instant readAt
) {
    public static MessageDto fromEntity(Message m) {
        return new MessageDto(
                m.getId(),
                m.getConversation().getId(),
                m.getSender().getId(),
                m.getSender().getFullName(),
                m.getBody(),
                m.getCreatedAt(),
                m.getReadAt()
        );
    }
}
