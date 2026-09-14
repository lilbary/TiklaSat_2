package com.gib.tiklasat.messaging;

import com.gib.tiklasat.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * İki kullanıcı arasındaki sohbet (Facebook tarzı: kişiye bağlı, ilana değil).
 *
 * Taraflar "gönderen/alıcı" değil "A/B" diye adlandırılıyor, çünkü sohbetin
 * yönü yok — ikisi de yazar. Hangi tarafın A hangisinin B olduğunu kimlik
 * sırası belirliyor (bkz. CHECK user_a_id &lt; user_b_id, V27).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Son mesajın zamanı. Bilerek denormalize: sohbet listesini sıralamak için
     * her sohbette messages tablosuna gitmek yerine burada tutuluyor.
     * Her yeni mesajda MessageService güncelliyor.
     */
    @Column(name = "last_message_at", nullable = false)
    private Instant lastMessageAt = Instant.now();

    /**
     * Sohbetin karşı tarafı. "Sohbetlerim" listesinde kimin adını
     * göstereceğimizi bulmak için — bakan kişi A da olabilir B de.
     */
    public User otherParty(UUID viewerId) {
        return userA.getId().equals(viewerId) ? userB : userA;
    }

    public boolean hasParticipant(UUID userId) {
        return userA.getId().equals(userId) || userB.getId().equals(userId);
    }
}
