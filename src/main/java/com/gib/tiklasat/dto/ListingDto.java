package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Listing;
import lombok.Data;

import java.util.UUID;

@Data
public class ListingDto {
    private UUID id;
    private UUID sellerId;
    private UUID categoryId;
    private String categoryName;
    private String title;
    private String description;
    private String status;
    private Integer viewCount;

    public static ListingDto fromEntity(Listing listing) {
        ListingDto dto = new ListingDto();
        dto.setId(listing.getId());
        dto.setSellerId(listing.getSeller().getId());
        dto.setCategoryId(listing.getCategory().getId());
        dto.setCategoryName(listing.getCategory().getName());
        dto.setTitle(listing.getTitle());
        dto.setDescription(listing.getDescription());
        dto.setStatus(listing.getStatus());
        dto.setViewCount(listing.getViewCount());
        return dto;
    }
}
