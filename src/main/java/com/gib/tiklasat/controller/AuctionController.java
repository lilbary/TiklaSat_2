package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.auction.AuctionCreateRequest;
import com.gib.tiklasat.dto.auction.AuctionDto;
import com.gib.tiklasat.dto.auction.BidRequest;
import com.gib.tiklasat.service.AuctionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<AuctionDto> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            Principal principal) {
        
        AuctionDto auction = auctionService.createAuction(request, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(auction);
    }

    @PostMapping("/bid")
    public ResponseEntity<Map<String, String>> placeBid(
            @Valid @RequestBody BidRequest request,
            Principal principal,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        // Teklif işlemi kilit altında çalışacak
        auctionService.placeBid(request, principal.getName(), ipAddress, userAgent);

        return ResponseEntity.ok(Map.of("message", "Teklifiniz başarıyla alındı!"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuctionDto> getAuction(@PathVariable UUID id) {
        return ResponseEntity.ok(auctionService.getAuction(id));
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
