package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping
    public ResponseEntity<Page<ListingDto>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(listingService.getAllListings(page, size));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ListingDto>> getListingsByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(listingService.getListingsByCategory(categoryId));
    }

    @PostMapping
    public ResponseEntity<ListingDto> createListing(@RequestBody ListingDto listingDto, Authentication authentication) {
        // JWT içindeki kullanıcı email'ini alıyoruz
        String sellerEmail = (String) authentication.getPrincipal();

        // Service metoduna artık bu email'i de göndereceğiz
        return ResponseEntity.ok(listingService.createListing(listingDto, sellerEmail));
    }
}
