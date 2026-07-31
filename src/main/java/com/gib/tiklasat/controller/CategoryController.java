package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.category.CategoryDto;
import com.gib.tiklasat.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Kategori işlemleri için kontrolcü sınıfı.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Ana kategorileri listeler.
     * @return Ana kategori listesi
     */
    @GetMapping({"", "/"})
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    /**
     * Alt kategorileri listeler.
     * @param parentId Üst kategori ID'si
     * @return Alt kategori listesi
     */
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDto>> getSubCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }
}
