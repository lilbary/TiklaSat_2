package com.gib.tiklasat.service;

import com.gib.tiklasat.entity.Auction;
import com.gib.tiklasat.exception.ConflictException;
import com.gib.tiklasat.exception.ForbiddenActionException;
import com.gib.tiklasat.repository.AuctionRepository;
import com.gib.tiklasat.repository.BidRepository;
import com.gib.tiklasat.dto.ListingDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.entity.Listing;
import com.gib.tiklasat.entity.Role;
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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.gib.tiklasat.entity.CategoryAttribute;
import com.gib.tiklasat.repository.CategoryAttributeRepository;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final CacheManager cacheManager;
    private final CategoryAttributeRepository categoryAttributeRepository;

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
        Listing listing = new Listing();
        listing.setTitle(dto.getTitle());
        listing.setDescription(dto.getDescription());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        listing.setCategory(category);

        User seller = userRepository.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        listing.setSeller(seller);

        // BR-U-003: ilk ilanla birlikte SELLER rolü eklenir, ayrı satıcı hesabı açılmaz.
        // Set olduğu için "ilk mi?" diye ayrıca sormaya gerek yok — zaten varsa
        // tekrar eklenmiyor, dolayısıyla ekstra bir sorgu da atmıyoruz.
        // Kullanıcı managed durumda; dirty checking commit sırasında INSERT'i kendisi atar.
        seller.addRole(Role.SELLER);

        //************************************************************************************

        // Frontend'den gelen dinamik özellikleri al (Örn: {"kilometre": 45000})
        Map<String, Object> attributes = dto.getAttributes();
        if (attributes == null) {
            attributes = Map.of(); // Boşsa boş map yap ki hata vermesin
        }

        // 1. Bu kategori için "zorunlu" olan alanların şablonunu çek -- Miras alma mevzusu burda da gecerli
        List<CategoryAttribute> requiredAttrs =
                categoryAttributeRepository.findByCategoryIdOrderBySortOrderAsc(category.getId());

        if (requiredAttrs.isEmpty() && category.getParent() != null) {
            Category parent = category.getParent();
            while (parent != null && requiredAttrs.isEmpty()) {
                requiredAttrs = categoryAttributeRepository
                        .findByCategoryIdOrderBySortOrderAsc(parent.getId());
                parent = parent.getParent();
            }
        }

        // 2. Zorunlu alanların gerçekten doldurulup doldurulmadığını kontrol et
        for (CategoryAttribute attr : requiredAttrs) {//tum kurallara bakalım
            if (attr.isRequired()) {//bu ozellik zorunluysa buraya gir
                Object value = attributes.get(attr.getName());

                // Eğer veri gelmediyse veya boş string ise hata fırlat
                if (value == null || value.toString().isBlank()) {
                    throw new IllegalArgumentException("'" + attr.getLabel() + "' alanı zorunludur.");
                }
            }
        }

        // 3. Her şey tamamsa JSONB kolonuna kaydetmek üzere Entity'ye set et
        listing.setAttributes(attributes);

        //***********************************************************************

        listing = listingRepository.save(listing);
        return ListingDto.fromEntity(listing);
    }



    /*
    @Transactional
    @CacheEvict(value = "listings_by_category", allEntries = true)
    public void deleteListing(UUID listingId, String sellerEmail) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı"));

        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new ForbiddenActionException("Bu ilan size ait değil, silemezsiniz!");
        }

        Optional<Auction> auctionOpt = auctionRepository.findByListingId(listingId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            if (bidRepository.existsByAuctionId(auction.getId())) {
                throw new ConflictException("Bu açık artırmaya teklif verilmiş, silemezsiniz!");
            }
            auction.setStatus("CANCELLED");
            auctionRepository.save(auction);
        }

        listing.setStatus("DELETED");
        listingRepository.save(listing);


    }*/
    
    @Transactional
    public void deleteListing(UUID listingId, String sellerEmail) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("İlan bulunamadı"));

        if (!listing.getSeller().getEmail().equals(sellerEmail)) {
            throw new ForbiddenActionException("Bu ilan size ait değil, silemezsiniz!");
        }

        Optional<Auction> auctionOpt = auctionRepository.findByListingId(listingId);
        if (auctionOpt.isPresent()) {
            Auction auction = auctionOpt.get();
            if (bidRepository.existsByAuctionId(auction.getId())) {
                throw new ConflictException("Bu açık artırmaya teklif verilmiş, silemezsiniz!");
            }
            auction.setStatus("CANCELLED");
            auctionRepository.save(auction);
        }

        listing.setStatus("DELETED");
        listingRepository.save(listing);

        // Sadece bu ilanın kategorisinin önbelleğini temizle — diğer kategoriler sıcak kalır
        UUID categoryId = listing.getCategory().getId();
        Cache cache = cacheManager.getCache("listings_by_category");
        if (cache != null) {
            cache.evict(categoryId);
        }
    }
}
