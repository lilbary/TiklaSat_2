package com.gib.tiklasat.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class AuctionCreateDto {
    private UUID listingId;
    private BigDecimal startingPrice;
    private Instant endTime;
    private BigDecimal reservePrice; // opsiyonel — boş bırakılabilir
}
