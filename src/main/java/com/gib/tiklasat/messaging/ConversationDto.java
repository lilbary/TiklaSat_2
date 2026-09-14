package com.gib.tiklasat.messaging;

import com.gib.tiklasat.user.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Sohbet listesindeki bir satır. userA/userB yerine "karşı taraf" olarak
 * düzleştiriliyor — arayüzün bakan kişinin hangi tarafta olduğunu
 * hesaplamasına gerek kalmıyor.
 */
public record ConversationDto(
        UUID id,
        UUID otherUserId,
        String otherUserName,
        Instant lastMessageAt
) {
    public static ConversationDto fromEntity(Conversation c, UUID viewerId) {
        User other = c.otherParty(viewerId);
        return new ConversationDto(
                c.getId(),
                other.getId(),
                other.getFullName(),
                c.getLastMessageAt()
        );
    }
}
