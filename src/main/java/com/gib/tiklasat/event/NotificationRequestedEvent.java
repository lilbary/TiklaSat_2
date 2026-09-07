package com.gib.tiklasat.event;

import java.util.List;
import java.util.UUID;

/**
 * "Şu bildirimler yazılsın" isteğini taşıyan olay.
 *
 * Entity DEĞİL, sadece kimlik ve hazır mesaj taşıyor: bu olay ana transaction
 * COMMIT olduktan sonra işlenecek ve o noktada persistence context kapalı
 * olduğu için entity'ler detached olur, lazy alanlarına erişilemez.
 * Mesaj bu yüzden olayı yayınlayan tarafta, veri hâlâ erişilebilirken hazırlanır.
 */
public record NotificationRequestedEvent(List<Item> items) {

    public record Item(UUID userId, UUID auctionId, String message) {}
}