package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.entity.ListingImage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class AuctionDto {
    private UUID id;
    private UUID listingId;
    private String listingTitle; // Ön yüzde kolaylık olsun diye ilanın başlığını da veriyoruz
    private String listingDescription;
    private UUID sellerId;
    private String sellerName;
    private UUID categoryId;
    private String categoryName;
    private String winnerName;
    private BigDecimal startingPrice; // SABİT — asla değişmez
    private BigDecimal currentPrice;
    private Boolean reserveMet; 
    private Instant startTime;
    private Instant endTime;
    private String status;
    private List<String> imageUrls;   // İlana ait fotoğraf URL'leri

    public static AuctionDto fromEntity(Auction auction, BigDecimal currentPrice) {
        AuctionDto dto = new AuctionDto();
        dto.setId(auction.getId());
        dto.setListingId(auction.getListing().getId());
        dto.setListingTitle(auction.getListing().getTitle());
        dto.setListingDescription(auction.getListing().getDescription());
        dto.setSellerId(auction.getListing().getSeller().getId());
        dto.setSellerName(auction.getListing().getSeller().getFullName());
        dto.setCategoryId(auction.getListing().getCategory().getId());
        dto.setCategoryName(auction.getListing().getCategory().getName());
        dto.setWinnerName(auction.getWinner() != null ? auction.getWinner().getFullName() : null);
        dto.setStartingPrice(auction.getStartingPrice());
        dto.setCurrentPrice(currentPrice);
        dto.setReserveMet(auction.getReservePrice() == null
                ? null
                : currentPrice.compareTo(auction.getReservePrice()) >= 0);
        dto.setStartTime(auction.getStartTime());
        dto.setEndTime(auction.getEndTime());
        dto.setStatus(auction.getStatus());

        // İlana ait fotoğrafların URL'lerini ekle
        List<String> urls = auction.getListing().getImages().stream()
                .map(ListingImage::getImageUrl)
                .toList();
        dto.setImageUrls(urls);

        return dto;
    }
}
