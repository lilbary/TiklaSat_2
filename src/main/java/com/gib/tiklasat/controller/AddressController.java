package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.AddressCreateDto;
import com.gib.tiklasat.dto.AddressDto;
import com.gib.tiklasat.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController // Spring'e "Bu sınıf internetten gelen istekleri (JSON olarak) karşılayacak" diyoruz.
@RequestMapping("/api/users/me/addresses") // Bu sınıftaki TÜM uçlar bu ana URL ile başlasın diyoruz.
@RequiredArgsConstructor
public class AddressController {

    // Aşçımızı (Service) içeri alıyoruz ki gelen siparişleri ona paslayalım.
    private final AddressService addressService;

    /**
     * 1. ADRESLERİ LİSTELE (GET)
     * URL: GET /api/users/me/addresses
     */
    @GetMapping
    public ResponseEntity<List<AddressDto>> getMyAddresses(Authentication authentication) {

        // Kimlik kartından (Token'dan) kullanıcının e-postasını okuyoruz.
        String email = (String) authentication.getPrincipal();

        // Aşçıya "Bu adamın adreslerini getir" diyoruz.
        List<AddressDto> addresses = addressService.getUserAddresses(email);

        // Gelen listeyi müşteriye (React'a) servis ediyoruz.
        return ResponseEntity.ok(addresses);
    }

    /**
     * 2. YENİ ADRES EKLE (POST)
     * URL: POST /api/users/me/addresses
     */
    @PostMapping
    public ResponseEntity<AddressDto> addAddress(
            Authentication authentication,
            @RequestBody AddressCreateDto dto) { // @RequestBody: "React'tan gelen JSON'u al, bu kargo kutusuna koy" demek.

        String email = (String) authentication.getPrincipal();

        // Aşçıya "Al bu e-posta ve al bu bilgiler, yeni adres yarat" diyoruz.
        AddressDto savedAddress = addressService.addAddress(email, dto);

        return ResponseEntity.ok(savedAddress);
    }

    /**
     * 3. ADRES GÜNCELLE (PUT)
     * URL: PUT /api/users/me/addresses/{id}
     * Örnek: PUT /api/users/me/addresses/12345-67890
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressDto> updateAddress(
            Authentication authentication,
            @PathVariable UUID id, // @PathVariable: "URL'in sonundaki 12345'i alıp bu değişkene koy" demek.
            @RequestBody AddressCreateDto dto) {

        String email = (String) authentication.getPrincipal();

        // Aşçıya "Bu kişinin, şu ID'li adresini, bu yeni bilgilerle güncelle" diyoruz.
        AddressDto updatedAddress = addressService.updateAddress(email, id, dto);

        return ResponseEntity.ok(updatedAddress);
    }

    /**
     * 4. ADRES SİL (DELETE)
     * URL: DELETE /api/users/me/addresses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            Authentication authentication,
            @PathVariable UUID id) {

        String email = (String) authentication.getPrincipal();

        // Aşçıya "Bu kişinin şu ID'li adresini çöpe at" diyoruz.
        addressService.deleteAddress(email, id);

        // İşlem başarılı oldu, geriye bir şey göndermemize gerek yok (Ok dönüşü yeterli).
        return ResponseEntity.ok().build();
    }
}