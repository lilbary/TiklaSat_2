package com.gib.tiklasat.service;

import com.gib.tiklasat.dto.CategoryDto;
import com.gib.tiklasat.entity.Category;
import com.gib.tiklasat.exception.ResourceNotFoundException;
import com.gib.tiklasat.repository.CategoryRepository;
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

    @Transactional(readOnly = true)
    @Cacheable(value = "categories")
    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
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
    public List<CategoryDto> getSubCategories(UUID parentId) {
        return categoryRepository.findByParentId(parentId).stream()
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

    private String generateSlug(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
