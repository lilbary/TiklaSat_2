package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.CategoryDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.CategoryRepository;
import com.gib.tiklasat.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ListingRepository listingRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrue().stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    // Sabit slug listesinden kategori bulma — "Haftanın Kategorileri" gibi bölümlerde
    // kategori ID'sini sabit kodlamak yerine (veritabanı sıfırlanınca değişir), kalıcı
    // slug üzerinden aramak için.
    @Transactional(readOnly = true)
    public List<CategoryDto> getCategoriesBySlugs(List<String> slugs) {
        return categoryRepository.findBySlugIn(slugs).stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "subcategories", key = "#parentId")
    public List<CategoryDto> getSubCategories(UUID parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId).stream()
                .map(CategoryDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public CategoryDto createCategory(CategoryDto dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSlug(dto.getSlug() != null ? dto.getSlug() : generateSlug(dto.getName()));
        
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        
        category = categoryRepository.save(category);
        return CategoryDto.fromEntity(category);
    }




    //UPDATE KATEGORİ
    @Transactional //aman int giderse falan sıkıntı olmasın. yarım yamalaak kaydetmeyek diye
    @CacheEvict(value = "categories", allEntries = true)//cachete tuttugumuz categoriler verisini degisikliktren sonr agunceller
    public CategoryDto updateCategory(UUID id, CategoryDto dto) {
        //sistemde degistirdigimiz kategorinin urlsi ve dtosu gelir. dto icinde name ve parentid var.
        //bunu da category objesine atıp sonra dto obejsindeki veriler ile guncelliyoruz.




        //Kategoriyi veritabanında bul
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Güncellenecek kategori bulunamadı!"));

        category.setName(dto.getName());
        // Admin güncelleme sırasında özel bir URL uzantısı (slug) göndermişse onu kullanır.
        // Göndermemişse, yeni girilen Kategori İsminden (dto.getName()) otomatik bir slug üretir.
        category.setSlug(dto.getSlug() != null ? dto.getSlug() : generateSlug(dto.getName()));

        // (Parent) Güncellemesi
        if (dto.getParentId() != null) {
            // Eğer yeni bir üst kategori seçilmişse, onu bulup bağla(bu ustg kategoei bizde categori dto da gelio)
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Belirtilen üst kategori bulunamadı!"));

            // Bir kategori kendi kendisinin ebeveyni olamaz!
            if (parent.getId().equals(category.getId())) {
                throw new IllegalArgumentException("Bir kategori kendi kendisinin üst kategorisi olamaz.");
            }

            category.setParent(parent);
        } else {
            // Eğer parentId null gönderilmişse, bu kategoriyi "Ana Kategori" (Root) yap
            category.setParent(null);
        }

        // 4. Kaydet ve geri dön
        category = categoryRepository.save(category);
        return CategoryDto.fromEntity(category);
    }








    //Deletekategori

    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    //duzelttik: (eski_hata)Silme işlemi için DTO'ya (yeni verilere) ihtiyacımız yok, sadece ID yeterli.
    // Geriye bir şey dönmesine gerek olmadığı için void yapıyoruz.

    public void deleteCategory(UUID id) {
// sadece id alır ve onu bulursa category degiskenine atar daha sonra sartlara gore  soft ya da hard delete yapar.
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Silinecek kategori bulunamadı!"));

        boolean aktifIlanVarMi = listingRepository.existsByCategoryIdAndStatus(id, "ACTIVE");
        boolean altKategoriVarMi = categoryRepository.existsByParentId(id);

        if (aktifIlanVarMi || altKategoriVarMi) {
            // Aktif ilan veya alt kategori var -> Silemeyiz, pasife alıyoruz (Soft Delete)
            category.setActive(false);
            categoryRepository.save(category);
        } else {
            // Hiçbiri yok -> Tamamen silebiliriz (Hard Delete)
            categoryRepository.delete(category);
        }
    }

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
