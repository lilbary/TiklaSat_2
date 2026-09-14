package com.gib.tiklasat.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gib.tiklasat.common.ForbiddenActionException;
import com.gib.tiklasat.common.ResourceNotFoundException;
import com.gib.tiklasat.outbox.OutboxEvent;
import com.gib.tiklasat.outbox.OutboxEventRepository;
import com.gib.tiklasat.user.User;
import com.gib.tiklasat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    /** Sohbet listesi ve mesaj sayfası için üst sınır — istemci daha fazlasını isteyemez. */
    private static final int MAX_PAGE_SIZE = 50;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------------
    // YAZMA
    // ------------------------------------------------------------------

    @Transactional
    public MessageDto sendMessage(String senderEmail, UUID recipientId, String body) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Gönderen bulunamadı!"));

        if (sender.getId().equals(recipientId)) {
            throw new ForbiddenActionException("Kendinize mesaj gönderemezsiniz.");
        }

        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Alıcı bulunamadı!"));

        Conversation conversation = findOrCreateConversation(sender, recipient);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setBody(body);

        // saveAndFlush: @CreationTimestamp ile üretilen createdAt'in DTO'ya
        // konmadan önce dolması gerekiyor; save() tek başına INSERT'i
        // transaction sonuna ertelerdi.
        message = messageRepository.saveAndFlush(message);

        // Sohbet listesinin sıralaması bu alana dayanıyor.
        conversation.setLastMessageAt(message.getCreatedAt());

        MessageDto dto = MessageDto.fromEntity(message);
        publishToRecipient(recipientId, dto);
        return dto;
    }

    /**
     * İki kullanıcı arasındaki sohbeti bulur, yoksa oluşturur.
     *
     * Kanonik sıralama burada uygulanıyor: küçük UUID her zaman userA olur.
     * Böylece kim başlatırsa başlatsın aynı satıra denk geliyor ve
     * (A,B) ile (B,A) iki ayrı sohbet oluşmuyor. Veritabanı bunu
     * CHECK (user_a_id &lt; user_b_id) ile ayrıca zorluyor.
     */
    private Conversation findOrCreateConversation(User one, User two) {
        boolean oneIsFirst = compareAsPostgres(one.getId(), two.getId()) < 0;
        User userA = oneIsFirst ? one : two;
        User userB = oneIsFirst ? two : one;

        return conversationRepository.findByUserAIdAndUserBId(userA.getId(), userB.getId())
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setUserA(userA);
                    c.setUserB(userB);
                    // Yarış durumu (iki kişi aynı anda birbirine ilk mesajı yazarsa)
                    // uq_conversation tarafından yakalanır ve istek hata döner.
                    // Burada yakalayıp tekrar okumayı DENEMİYORUZ: PostgreSQL'de
                    // başarısız bir ifade tüm transaction'ı abort ettiği için
                    // catch içinde atılacak SELECT de çalışmaz. Nadir bir durum,
                    // kullanıcı tekrar gönderdiğinde sohbet artık mevcut olur.
                    return conversationRepository.saveAndFlush(c);
                });
    }

    /**
     * UUID'leri PostgreSQL ile AYNI şekilde sıralar.
     *
     * Java'nın UUID.compareTo'su en anlamlı 64 biti İŞARETLİ long olarak
     * karşılaştırır; ilk biti 1 olan UUID'ler negatif çıkıp sıralamanın başına
     * geçer. PostgreSQL ise baytları işaretsiz karşılaştırır. İki taraf farklı
     * sıralarsa kanonikleştirme bozulur ve CHECK (user_a_id &lt; user_b_id)
     * satırı reddeder — ki tam olarak bu yaşandı.
     */
    private static int compareAsPostgres(UUID a, UUID b) {
        int cmp = Long.compareUnsigned(a.getMostSignificantBits(), b.getMostSignificantBits());
        return cmp != 0 ? cmp
                : Long.compareUnsigned(a.getLeastSignificantBits(), b.getLeastSignificantBits());
    }

    /**
     * Mesajı alıcının kişisel kanalına bırakır. Outbox deseni: WebSocket'e
     * doğrudan göndermek yerine aynı transaction'da bir satır yazıyoruz;
     * OutboxPublisherJob saniyede bir alıp RabbitMQ'ya iletiyor. Böylece
     * "mesaj veritabanında var ama karşı tarafa hiç gitmedi" durumu oluşmuyor.
     */
    private void publishToRecipient(UUID recipientId, MessageDto dto) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventType("MESSAGE_SENT");
            event.setPayload(objectMapper.writeValueAsString(
                    Map.of(
                            "destination", "/topic/messages." + recipientId,
                            "payload", dto
                    )
            ));
            outboxEventRepository.save(event);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Mesaj outbox kaydı oluşturulamadı", e);
        }
    }

    @Transactional
    public int markAsRead(String readerEmail, UUID conversationId) {
        User reader = userRepository.findByEmail(readerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));
        requireParticipant(conversationId, reader.getId());

        return messageRepository.markAsRead(conversationId, reader.getId(), Instant.now());
    }

    // ------------------------------------------------------------------
    // OKUMA
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ConversationDto> getMyConversations(String userEmail, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));

        return conversationRepository
                .findByParticipant(user.getId(), PageRequest.of(0, clamp(limit)))
                .stream()
                .map(c -> ConversationDto.fromEntity(c, user.getId()))
                .toList();
    }

    /**
     * Sohbetin mesajları, yeniden eskiye.
     *
     * before null ise en son mesajlardan başlar; doluysa "o andan öncekiler"
     * gelir. Keyset sayfalama: OFFSET yerine zaman damgasıyla ilerliyoruz,
     * böylece kullanıcı ne kadar yukarı kaydırırsa kaydırsın sorgu aynı
     * hızda kalıyor.
     */
    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(String userEmail, UUID conversationId, Instant before, int limit) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));
        requireParticipant(conversationId, user.getId());

        PageRequest page = PageRequest.of(0, clamp(limit));
        List<Message> messages = (before == null)
                ? messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, page)
                : messageRepository.findByConversationIdAndCreatedAtBeforeOrderByCreatedAtDesc(
                        conversationId, before, page);

        List<MessageDto> result = new ArrayList<>(messages.size());
        for (Message m : messages) {
            result.add(MessageDto.fromEntity(m));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public long countUnread(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı!"));
        return messageRepository.countUnreadForUser(user.getId());
    }

    // ------------------------------------------------------------------
    // YARDIMCILAR
    // ------------------------------------------------------------------

    /**
     * Sohbete erişim kontrolü. Sohbet kimliği tahmin edilemez olsa da
     * yetkilendirmeyi "bilinmesi zor" varsayımına bırakmıyoruz.
     */
    private void requireParticipant(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Sohbet bulunamadı!"));
        if (!conversation.hasParticipant(userId)) {
            throw new ForbiddenActionException("Bu sohbete erişim yetkiniz yok.");
        }
    }

    /** İstemcinin gönderdiği limit'i makul bir aralığa sıkıştırır. */
    private int clamp(int limit) {
        if (limit <= 0) return 20;
        return Math.min(limit, MAX_PAGE_SIZE);
    }
}
