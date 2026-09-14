package com.gib.tiklasat.messaging;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

    /**
     * Sohbetin son mesajları, yeniden eskiye.
     * idx_messages_conversation (conversation_id, created_at DESC) ile birebir
     * eşleşiyor: hem atlama hem sıralama index'ten geliyor.
     */
    @EntityGraph(attributePaths = "sender")
    List<Message> findByConversationIdOrderByCreatedAtDesc(UUID conversationId, Pageable pageable);

    /**
     * Keyset sayfalama: "şu andan öncekiler". OFFSET kullanmıyoruz — OFFSET 10000
     * demek veritabanının 10.000 satırı okuyup atması demek. Bu şekilde index'te
     * doğrudan ilgili noktaya atlanıyor.
     */
    @EntityGraph(attributePaths = "sender")
    List<Message> findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
            UUID conversationId, Instant before, Pageable pageable);

    /**
     * Okunmamış rozeti: karşı tarafın gönderdiği, henüz okunmamış mesajlar.
     * idx_messages_unread kısmi index'ini kullanıyor.
     */
    @Query("""
           SELECT count(m) FROM Message m
           WHERE (m.conversation.userA.id = :userId OR m.conversation.userB.id = :userId)
             AND m.sender.id <> :userId
             AND m.readAt IS NULL
           """)
    long countUnreadForUser(@Param("userId") UUID userId);

    /**
     * Sohbet açıldığında karşı tarafın mesajlarını okundu işaretle.
     * Tek UPDATE ile: entity'leri tek tek yükleyip değiştirmek yerine
     * doğrudan veritabanında güncelliyoruz.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
           UPDATE Message m SET m.readAt = :now
           WHERE m.conversation.id = :conversationId
             AND m.sender.id <> :readerId
             AND m.readAt IS NULL
           """)
    int markAsRead(@Param("conversationId") UUID conversationId,
                   @Param("readerId") UUID readerId,
                   @Param("now") Instant now);
}
