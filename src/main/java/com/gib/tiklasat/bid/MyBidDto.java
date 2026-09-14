package com.gib.tiklasat.bid;

import com.gib.tiklasat.auction.AuctionDto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MyBidDto {
    private AuctionDto auction;
    private BigDecimal myBidAmount; // Bu auction'a verdiğim EN YÜKSEK teklif
    private boolean winning;        // Şu an ben mi önde gidiyorum?
}