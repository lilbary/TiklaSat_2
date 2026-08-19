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
    private String listingDescription;
    private String sellerName;
    private BigDecimal startingPrice; // SABİT — asla değişmez
    private BigDecimal currentPrice;  // DEĞİŞKEN — en yüksek teklif (yoksa startingPrice)
    private Instant startTime;
    private Instant endTime;
    private String status;

    public static AuctionDto fromEntity(Auction auction, BigDecimal currentPrice) {
        AuctionDto dto = new AuctionDto();
        dto.setId(auction.getId());
        dto.setListingId(auction.getListing().getId());
        dto.setListingTitle(auction.getListing().getTitle());
        dto.setListingDescription(auction.getListing().getDescription());
        dto.setSellerName(auction.getListing().getSeller().getFullName());
        dto.setStartingPrice(auction.getStartingPrice());
        dto.setCurrentPrice(currentPrice);
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setStatus(auction.getStatus());
        return dto;
    }
}
