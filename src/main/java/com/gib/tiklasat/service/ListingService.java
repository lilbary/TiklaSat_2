package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.CategoryRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // NOT: Burası @Cacheable OLAMAZ — Page (PageImpl) Redis'e yazılabiliyor ama
    // geri okunurken Jackson'ın kurabileceği bir constructor'ı olmadığı için
    // ikinci istekte "Cannot construct instance of PageImpl" hatasıyla patlıyor.
    @Transactional(readOnly = true)
    public Page<ListingDto> getAllListings(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Listing> listingPage = listingRepository.findAll(pageable);
        return listingPage.map(ListingDto::fromEntity);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "listings_by_category", key = "#categoryId")
    public List<ListingDto> getListingsByCategory(UUID categoryId) {
        return listingRepository.findByCategoryId(categoryId).stream()
                .map(ListingDto::fromEntity)
                .collect(Collectors.toList());
    }
    @Transactional
    @CacheEvict(value = "listings_by_category", allEntries = true)
    public ListingDto createListing(ListingDto dto, String sellerEmail) {
        // 1. Senin yazdığın Fiyat Kontrolü
        if (dto.getPrice() == null || dto.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fiyat sıfır veya negatif olamaz!");
        }
        Listing listing = new Listing();
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        listing.setPrice(dto.getPrice()); // <-- Senin eklediğin kısım
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        listing.setCategory(category);
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        listing.setSeller(seller);
        listing = listingRepository.save(listing);
        return ListingDto.fromEntity(listing);
    }

}
