package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AuctionDto;
import com.gib.tiklasat.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    // FAVORİYE EKLE — kalbe ilk basış
    @PostMapping("/{auctionId}")
    public ResponseEntity<Void> addFavorite(@PathVariable UUID auctionId, Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        favoriteService.addFavorite(auctionId, userEmail);
        return ResponseEntity.ok().build();
    }

    // FAVORİDEN ÇIKAR — kalbe ikinci basış
    @DeleteMapping("/{auctionId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID auctionId, Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        favoriteService.removeFavorite(auctionId, userEmail);
        return ResponseEntity.ok().build();
    }

    // FAVORİLERİMİ LİSTELE
    @GetMapping("/mine")
    public ResponseEntity<List<AuctionDto>> getMyFavorites(Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(favoriteService.getMyFavorites(userEmail));
    }
}