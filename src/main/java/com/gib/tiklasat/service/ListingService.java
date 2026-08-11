package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.CategoryRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ListingDto> getAllListings() {
        return listingRepository.findAll().stream()
                .map(ListingDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ListingDto> getListingsByCategory(UUID categoryId) {
        return listingRepository.findByCategoryId(categoryId).stream()
                .map(ListingDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ListingDto createListing(ListingDto dto) {
        Listing listing = new Listing();
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());
        
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        listing.setCategory(category);
        
        // Şimdilik satıcıyı dto'dan alıyoruz (Güvenlik olmadığı için)
        User seller = userRepository.findById(dto.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        listing.setSeller(seller);
        
        listing = listingRepository.save(listing);
        return ListingDto.fromEntity(listing);
    }
}
