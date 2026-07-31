package com.gib.tiklasat.controller;

import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.ListingImage;
import com.gib.tiklasat.repository.ListingImageRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * İlan Görselleri API Uç Noktası.
 * Kullanıcıların ilanlarına fotoğraf yüklemesini sağlar.
 */
@RestController
@RequestMapping("/api/listings/{listingId}/images")
@RequiredArgsConstructor
public class ListingImageController {

    private final FileStorageService fileStorageService;
    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;

    @PostMapping
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable UUID listingId,
            @RequestParam("file") MultipartFile file,
            Principal principal) {

        // İlanı bul
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new IllegalArgumentException("İlan bulunamadı"));

        // İlan sahibi mi kontrol et
        if (!listing.getSeller().getEmail().equals(principal.getName())) {
            throw new IllegalStateException("Sadece kendi ilanınıza fotoğraf yükleyebilirsiniz!");
        }

        // Fotoğraf sayısını kontrol et (Maksimum 15 fotoğraf - BR-U-009)
        long currentImageCount = listingImageRepository.countByListingId(listingId);
        if (currentImageCount >= 15) {
            throw new IllegalStateException("Bir ilana en fazla 15 fotoğraf yüklenebilir!");
        }

        // Dosyayı diske kaydet
        String storageKey = fileStorageService.storeFile(file);

        // Veritabanına kaydet
        ListingImage image = new ListingImage();
        image.setListing(listing);
        image.setStorageKey(storageKey);
        image.setContentType(file.getContentType());
        image.setSizeBytes((int) file.getSize());
        image.setSortOrder((short) currentImageCount); // Sırasını belirle
        
        // İlk yüklenen fotoğrafı "Kapak Fotoğrafı" yap
        image.setIsCover(currentImageCount == 0);

        listingImageRepository.save(image);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Fotoğraf başarıyla yüklendi",
                "storageKey", storageKey
        ));
    }
}
