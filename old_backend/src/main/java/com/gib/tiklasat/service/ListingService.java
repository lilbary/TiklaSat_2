package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.listing.ListingCreateRequest;
import com.gib.tiklasat.dto.listing.ListingDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.entity.City;
import com.gib.tiklasat.entity.District;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.User;
import com.gib.tiklasat.repository.CategoryRepository;
import com.gib.tiklasat.repository.CityRepository;
import com.gib.tiklasat.repository.DistrictRepository;
import com.gib.tiklasat.repository.ListingRepository;
import com.gib.tiklasat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * İlan işlemleri servisi.
 */
@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;

    /**
     * Yeni bir taslak ilan oluşturur.
     * 
     * @param request ilan bilgileri
     * @param sellerEmail ilanı oluşturan kullanıcının e-posta adresi
     * @return oluşturulan ilanın DTO gösterimi
     */
    @Transactional
    public ListingDto createDraftListing(ListingCreateRequest request, String sellerEmail) {
        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + sellerEmail));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.categoryId()));

        City city = cityRepository.findById(request.cityId().shortValue())
                .orElseThrow(() -> new IllegalArgumentException("City not found with id: " + request.cityId()));

        District district = districtRepository.findById(request.districtId().intValue())
                .orElseThrow(() -> new IllegalArgumentException("District not found with id: " + request.districtId()));

        Listing listing = new Listing();
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setCategory(category);
        listing.setCity(city);
        listing.setDistrict(district);
        listing.setSeller(seller);
        listing.setStatus("DRAFT");
        
        Listing savedListing = listingRepository.save(listing);

        return mapToDto(savedListing);
    }

    private ListingDto mapToDto(Listing listing) {
        return ListingDto.builder()
                .id(listing.getId())
                .title(listing.getTitle())
                .status(listing.getStatus())
                .categoryName(listing.getCategory() != null ? listing.getCategory().getName() : null)
                .cityName(listing.getCity() != null ? listing.getCity().getName() : null)
                .districtName(listing.getDistrict() != null ? listing.getDistrict().getName() : null)
                .createdAt(listing.getCreatedAt())
                .build();
    }
}
