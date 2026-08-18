package com.gib.tiklasat.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BidCreateDto {
    private UUID auctionId;
    private BigDecimal amount;
    // bidderId ARTIK YOK — kimin teklif verdiği JWT'den (Authentication) çıkarılıyor.
    // İstemcinin "ben X kullanıcısıyım" diye başkası adına teklif vermesi artık mümkün değil.
}
