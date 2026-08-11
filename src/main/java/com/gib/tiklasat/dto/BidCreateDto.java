package com.gib.tiklasat.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BidCreateDto {
    private UUID auctionId;
    private UUID bidderId; // (İleride güvenlik eklendiğinde bu parametreyi Token'dan çekeceğiz)
    private BigDecimal amount;
}
