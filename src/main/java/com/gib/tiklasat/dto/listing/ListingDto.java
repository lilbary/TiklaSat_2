package com.gib.tiklasat.dto.listing;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * İlan bilgilerini taşıyan DTO.
 */
@Data
@Builder
public class ListingDto {
    private UUID id;
    private String title;
    private String status;
    private String categoryName;
    private String cityName;
    private String districtName;
    private Instant createdAt;
}
