package com.gib.tiklasat.messaging;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Mesajlaşma uçları.
 *
 * Hepsi giriş yapmış kullanıcı gerektiriyor; Authentication'ı Spring
 * dolduruyor ve getName() JWT'deki e-postayı veriyor. Sohbete erişim
 * yetkisi servis katmanında kontrol ediliyor.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** Mesaj gönder. Sohbet yoksa ilk mesajda oluşturulur. */
    @PostMapping
    public ResponseEntity<MessageDto> send(@Valid @RequestBody SendMessageRequest request,
                                           Authentication authentication) {
        return ResponseEntity.ok(messageService.sendMessage(
                authentication.getName(), request.getRecipientId(), request.getBody()));
    }

    /** Sohbet listesi, en son konuşulan üstte. */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> conversations(
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getMyConversations(authentication.getName(), limit));
    }

    /**
     * Bir sohbetin mesajları, yeniden eskiye.
     * before doluysa "o andan öncekiler" gelir — yukarı kaydırma bunu kullanıyor.
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<List<MessageDto>> messages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant before,
            @RequestParam(defaultValue = "20") int limit,
            Authentication authentication) {
        return ResponseEntity.ok(messageService.getMessages(
                authentication.getName(), conversationId, before, limit));
    }

    /** Sohbet açıldığında karşı tarafın mesajlarını okundu işaretle. */
    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Map<String, Integer>> markRead(@PathVariable UUID conversationId,
                                                         Authentication authentication) {
        int updated = messageService.markAsRead(authentication.getName(), conversationId);
        return ResponseEntity.ok(Map.of("markedAsRead", updated));
    }

    /** Sağ alttaki kutunun rozeti için toplam okunmamış sayısı. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(authentication.getName())));
    }
}
