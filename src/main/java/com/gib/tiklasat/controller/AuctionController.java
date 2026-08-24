package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AuctionCreateDto;
import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    // YENİ AÇIK ARTIRMA BAŞLATMA
    @PostMapping
    public ResponseEntity<AuctionDto> createAuction(@RequestBody AuctionCreateDto request, Authentication authentication) {
        String sellerEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(
            auctionService.createAuction(request.getListingId(), request.getStartingPrice(), request.getEndTime(), sellerEmail)
        );
    }
    // TÜM AÇIK ARTIRMALARI GETİR
    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<java.util.List<AuctionDto>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    // TEK BİR AÇIK ARTIRMAYI GETİR
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ResponseEntity<AuctionDto> getAuctionById(@org.springframework.web.bind.annotation.PathVariable java.util.UUID id) {
        return ResponseEntity.ok(auctionService.getAuctionById(id));
    }

    // ARAMA + KATEGORİ FİLTRESİ + SAYFALAMA
    @GetMapping("/search")
    public ResponseEntity<Page<AuctionDto>> searchAuctions(
            @RequestParam(required = false) String ara,
            @RequestParam(required = false) UUID kategori,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(auctionService.searchAuctions(ara, kategori, page, size));
    }
    
}
