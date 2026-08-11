package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Auction;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
public class AuctionDto {
    private UUID id;
    private UUID listingId;
    private String listingTitle; // Ön yüzde kolaylık olsun diye ilanın başlığını da veriyoruz
    private BigDecimal startingPrice;
    private Instant startTime;
    private Instant endTime;
    private String status;

    public static AuctionDto fromEntity(Auction auction) {
        AuctionDto dto = new AuctionDto();
        dto.setId(auction.getId());
        dto.setListingId(auction.getListing().getId());
        dto.setListingTitle(auction.getListing().getTitle());
        dto.setStartingPrice(auction.getStartingPrice());
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setStatus(auction.getStatus());
        return dto;
    }
}
