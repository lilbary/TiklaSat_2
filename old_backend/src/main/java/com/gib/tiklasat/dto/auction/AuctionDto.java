package com.gib.tiklasat.dto.auction;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Açık artırma detaylarını müşteriye dönen DTO.
 */
@Getter
@Builder
public class AuctionDto {
    private UUID id;
    private UUID listingId;
    private BigDecimal startPrice;
    private BigDecimal currentPrice;
    private Integer bidCount;
    private Instant startsAt;
    private Instant endsAt;
    private String status;
    private boolean reservePriceMet; // Kullanıcı taban fiyat miktarını bilmez, sadece geçilip geçilmediğini bilir!
}
