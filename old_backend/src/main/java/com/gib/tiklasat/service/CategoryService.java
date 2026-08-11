package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.category.CategoryDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Kategori işlemleri için servis sınıfı.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Ana kategorileri getirir (parentId null olanlar).
     * @return Ana kategori listesi
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIdIsNullAndIsActiveTrue()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Belirtilen üst kategoriye ait alt kategorileri getirir.
     * @param parentId Üst kategori ID'si
     * @return Alt kategori listesi
     */
    @Transactional(readOnly = true)
    public List<CategoryDto> getSubCategories(UUID parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Kategori entity'sini DTO'ya çevirir.
     * @param category Kategori entity'si
     * @return Kategori DTO'su
     */
    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .icon(category.getIcon())
                .isLeaf(category.getIsLeaf())
                .build();
    }
}
