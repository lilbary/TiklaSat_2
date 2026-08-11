package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.listing.ListingCreateRequest;
import com.gib.tiklasat.dto.listing.ListingDto;
import com.gib.tiklasat.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * İlan işlemleri için REST API uç noktaları.
 */
@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    /**
     * Yeni bir taslak ilan oluşturur.
     * 
     * @param request ilan oluşturma isteği
     * @param principal oturum açmış kullanıcı bilgisi
     * @return oluşturulan ilanın DTO gösterimi
     */
    @PostMapping({"/", ""})
    public ResponseEntity<ListingDto> createDraftListing(
            @Valid @RequestBody ListingCreateRequest request,
            Principal principal) {
        String sellerEmail = principal.getName();
        ListingDto createdListing = listingService.createDraftListing(request, sellerEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdListing);
    }
}
