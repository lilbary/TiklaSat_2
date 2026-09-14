package com.gib.tiklasat.category;

import com.gib.tiklasat.category.CategoryAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, UUID> {

    List<CategoryAttribute> findByCategoryIdOrderBySortOrderAsc(UUID categoryId);
}