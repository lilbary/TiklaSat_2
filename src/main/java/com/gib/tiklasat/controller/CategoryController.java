package com.gib.tiklasat.controller;

import com.gib.tiklasat.dto.CategoryDto;
import com.gib.tiklasat.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    @GetMapping("/by-slugs")
    public ResponseEntity<List<CategoryDto>> getCategoriesBySlugs(@RequestParam List<String> slugs) {
        return ResponseEntity.ok(categoryService.getCategoriesBySlugs(slugs));
    }

    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryDto>> getSubCategories(@PathVariable UUID parentId) {
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDto));
    }

    // --- Kategori Güncelleme Endpoint'i ---
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable UUID id, @RequestBody CategoryDto categoryDto) {
        // Gelen URL'deki ID'yi ve Gövdedeki (Body) DTO'yu alıp Service katmanına paslıyoruz.
        CategoryDto updatedCategory = categoryService.updateCategory(id, categoryDto);

        // İşlem başarılı olursa, güncellenmiş kategoriyi "200 OK" statüsüyle geri dönüyoruz.
        return ResponseEntity.ok(updatedCategory);
    }

    // --- Kategori Silme/Pasife Alma Endpoint'i ---
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        // Sadece URL'deki ID'yi alıp Service'teki o meşhur BR-C-007 kurallı metodumuza gönderiyoruz.
        categoryService.deleteCategory(id);

        // İşlem başarıyla bittiğinde geriye bir data dönmemize gerek yok.
        // REST standartlarına göre başarılı silme işlemleri "204 No Content" döner.
        return ResponseEntity.noContent().build();
    }
}
