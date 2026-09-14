package com.gib.tiklasat.messaging;

import com.gib.tiklasat.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Kim yazdı. Alıcıyı ayrıca tutmuyoruz — sohbetin iki tarafı belli,
     * gönderen belliyse alıcı da bellidir. Tutsaydık tutarsız hale
     * gelebilecek fazladan bir veri olurdu.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 2000)
    private String body;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Okunma zamanı. Boolean yerine zaman damgası: aynı maliyetle hem
     * "okundu mu" (null ise hayır) hem "ne zaman okundu" bilgisini veriyor.
     */
    @Column(name = "read_at")
    private Instant readAt;

    public boolean isRead() {
        return readAt != null;
    }
}
