package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.ListingImage;
import com.gib.tiklasat.repository.ListingImageRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.service.FileStorageService;
import com.gib.tiklasat.service.ListingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final FileStorageService fileStorageService;
    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;

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

    // FOTOĞRAF YÜKLEME
    @PostMapping("/{id}/images")
    public ResponseEntity<?> uploadImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        // 1. İlanı bul
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("İlan bulunamadı"));

        // 2. Güvenlik: Sadece ilanın sahibi fotoğraf yükleyebilir
        String userEmail = (String) authentication.getPrincipal();
        if (!listing.getSeller().getEmail().equals(userEmail)) {
            return ResponseEntity.status(403).body(Map.of("message", "Bu ilan size ait değil!"));
        }

        // 3. Dosyayı diske kaydet
        String imageUrl = fileStorageService.storeFile(file);

        // 4. Veritabanına resim kaydını ekle
        ListingImage image = new ListingImage();
        image.setListing(listing);
        image.setImageUrl(imageUrl);
        image.setPrimary(listing.getImages().isEmpty()); // İlk yüklenen = kapak fotoğrafı
        listingImageRepository.save(image);

        return ResponseEntity.ok(Map.of("message", "Fotoğraf yüklendi", "imageUrl", imageUrl));
    }

    // BİR İLANIN FOTOĞRAFLARINI GETİR
    @GetMapping("/{id}/images")
    public ResponseEntity<List<String>> getListingImages(@PathVariable UUID id) {
        List<String> imageUrls = listingImageRepository.findByListingId(id).stream()
                .map(ListingImage::getImageUrl)
                .toList();
        return ResponseEntity.ok(imageUrls);
    }

    // İLAN SİLME (soft delete — durumu DELETED/CANCELLED yapar, gerçek DB DELETE değil)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteListing(@PathVariable UUID id, Authentication authentication) {
        String sellerEmail = (String) authentication.getPrincipal();
        listingService.deleteListing(id, sellerEmail);
        return ResponseEntity.ok(Map.of("message", "İlan silindi"));
    }
}
