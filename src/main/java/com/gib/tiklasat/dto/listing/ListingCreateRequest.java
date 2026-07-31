package com.gib.tiklasat.dto.listing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * İlan oluşturma isteği için DTO.
 */
public record ListingCreateRequest(
        @NotBlank
        @Size(max = 70)
        String title,

        @NotBlank
        @Size(max = 3000)
        String description,

        @NotNull
        UUID categoryId,

        @NotNull
        Long cityId,

        @NotNull
        Long districtId
) {
}
