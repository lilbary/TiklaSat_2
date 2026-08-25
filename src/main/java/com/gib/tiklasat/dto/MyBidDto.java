package com.gib.tiklasat.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyBidDto {
    private AuctionDto auction;
    private BigDecimal myBidAmount; // Bu auction'a verdiğim EN YÜKSEK teklif
    private boolean winning;        // Şu an ben mi önde gidiyorum?
}