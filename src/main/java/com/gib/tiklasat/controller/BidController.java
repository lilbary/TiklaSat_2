package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.BidCreateDto;
import com.gib.tiklasat.dto.BidDto;
import com.gib.tiklasat.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    // YENİ TEKLİF VERME
    @PostMapping
    public ResponseEntity<BidDto> placeBid(@RequestBody BidCreateDto request, Authentication authentication) {
        // authentication.getPrincipal(): JwtAuthenticationFilter'ın SecurityContextHolder'a
        // yazdığı, o an giriş yapmış kullanıcının email'i (bkz. JwtAuthenticationFilter.java).
        String bidderEmail = (String) authentication.getPrincipal();

        return ResponseEntity.ok(
            bidService.placeBid(request.getAuctionId(), bidderEmail, request.getAmount())
        );
    }
}
