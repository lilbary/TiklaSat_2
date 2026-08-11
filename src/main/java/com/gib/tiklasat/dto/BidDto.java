package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Bid;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class BidDto {
    private UUID id;
    private UUID auctionId;
    private UUID bidderId;
    private String bidderName; // Ön yüz için teklif verenin ismini de gönderelim
    private BigDecimal amount;
    private Instant createdAt;

    public static BidDto fromEntity(Bid bid) {
        BidDto dto = new BidDto();
        dto.setId(bid.getId());
        dto.setAuctionId(bid.getAuction().getId());
        dto.setBidderId(bid.getBidder().getId());
        dto.setBidderName(bid.getBidder().getFullName());
        dto.setAmount(bid.getAmount());
        dto.setCreatedAt(bid.getCreatedAt());
        return dto;
    }
}
