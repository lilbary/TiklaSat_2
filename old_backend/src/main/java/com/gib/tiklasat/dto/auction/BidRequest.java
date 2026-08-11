package com.gib.tiklasat.dto.auction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Teklif verme isteği.
 */
@Getter
@Setter
public class BidRequest {

    @NotNull(message = "Açık artırma ID boş olamaz")
    private UUID auctionId;

    @NotNull(message = "Teklif miktarı boş olamaz")
    @DecimalMin(value = "0.01", message = "Teklif miktarı 0'dan büyük olmalıdır")
    private BigDecimal amount;
    
    // Otomatik teklif limiti (Proxy Bid) için kullanılacak.
    private BigDecimal maxAmount; 
}
