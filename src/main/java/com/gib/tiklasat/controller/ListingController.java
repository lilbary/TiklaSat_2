package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping
    public ResponseEntity<List<ListingDto>> getAllListings() {
        return ResponseEntity.ok(listingService.getAllListings());
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
