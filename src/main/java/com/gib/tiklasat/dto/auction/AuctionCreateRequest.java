package com.gib.tiklasat.dto.auction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Açık artırma başlatma isteği.
 */
@Getter
@Setter
public class AuctionCreateRequest {

    @NotNull(message = "İlan ID boş olamaz")
    private UUID listingId;

    @NotNull(message = "Başlangıç fiyatı boş olamaz")
    @DecimalMin(value = "0.01", message = "Başlangıç fiyatı 0'dan büyük olmalıdır")
    private BigDecimal startPrice;

    private BigDecimal reservePrice; // Gizli taban fiyat (opsiyonel)

    @NotNull(message = "Bitiş tarihi boş olamaz")
    @Future(message = "Bitiş tarihi gelecekte bir zaman olmalıdır")
    private Instant endsAt;
}
