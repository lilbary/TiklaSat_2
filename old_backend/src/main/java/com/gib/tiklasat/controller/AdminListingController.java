package com.gib.tiklasat.controller;

import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Yönetici (Moderatör) Paneli API Uç Noktaları.
 * İlanları onaylamak veya reddetmek için kullanılır.
 */
@RestController
@RequestMapping("/api/admin/listings")
@RequiredArgsConstructor
public class AdminListingController {

    private final ListingRepository listingRepository;

    /**
     * İlanı onaylar. (Sadece ADMIN ve MODERATOR yetkisi olanlar yapabilir).
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public ResponseEntity<Map<String, String>> approveListing(@PathVariable UUID id) {
        
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        if (!"PENDING_REVIEW".equals(listing.getStatus()) && !"DRAFT".equals(listing.getStatus())) {
            throw new IllegalStateException("Bu ilan şu anda onaylanamaz! Mevcut Durum: " + listing.getStatus());
        }

        listing.setStatus("APPROVED");
        listing.setPublishedAt(Instant.now());
        // Normalde burada "moderated_by" alanına işlemi yapan yöneticinin ID'si de kaydedilmelidir.
        
        listingRepository.save(listing);

        return ResponseEntity.ok(Map.of("message", "İlan başarıyla onaylandı ve yayına alındı."));
    }

    /**
     * İlanı reddeder.
     */
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Transactional
    public ResponseEntity<Map<String, String>> rejectListing(
            @PathVariable UUID id, 
            @RequestParam String reason) {
        
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        listing.setStatus("REJECTED");
        listing.setModerationNote(reason);
        
        listingRepository.save(listing);

        return ResponseEntity.ok(Map.of("message", "İlan reddedildi. Sebep: " + reason));
    }
}
