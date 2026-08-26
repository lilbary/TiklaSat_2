package com.gib.tiklasat.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReceivedBidDto {
    private AuctionDto auction;
    private BigDecimal topBidAmount;   // İlanıma verilen EN YÜKSEK teklif
    private String topBidderName;      // Bu teklifi kim verdi
}
