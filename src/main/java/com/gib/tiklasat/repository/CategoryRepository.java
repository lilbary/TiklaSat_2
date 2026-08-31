package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentIsNull();
    List<Category> findByParentId(UUID parentId);
    List<Category> findBySlugIn(List<String> slugs);
}
