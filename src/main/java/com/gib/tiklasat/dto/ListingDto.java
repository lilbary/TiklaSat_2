package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Listing;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
import java.util.Map;

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
    private Instant createdAt;
    private Map<String, Object> attributes;
    //String:Key Object:Value
    //veri tiplerini kendisi algılar
    //dogrulama yaparken keyvalue mevzusu ile kolayca arama yapabilir  //attributes.get("kilometre")
    //json ı jsonb ye cevirmeye yarar

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
        dto.setCreatedAt(listing.getCreatedAt());
        dto.setAttributes(listing.getAttributes());
        return dto;
    }
}
