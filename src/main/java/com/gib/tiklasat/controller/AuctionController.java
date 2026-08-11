package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AuctionCreateDto;
import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.service.AuctionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    // YENİ AÇIK ARTIRMA BAŞLATMA
    @PostMapping
    public ResponseEntity<AuctionDto> createAuction(@RequestBody AuctionCreateDto request) {
        // Müşteriden siparişi (request) alıp, doğrudan Aşçıya (Service) teslim ediyoruz.
        return ResponseEntity.ok(
            auctionService.createAuction(request.getListingId(), request.getStartingPrice(), request.getEndTime())
        );
    }
}



public List<Employee> getEmployeeWithParams(@requestparam (name = "firstnaem") String firstname)