package com.gib.tiklasat.category;

import com.gib.tiklasat.category.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentIsNullAndIsActiveTrue();              //parents
    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);       //cocuklar
    boolean existsByParentId(UUID parentId);
    List<Category> findBySlugIn(List<String> slugs);
}
