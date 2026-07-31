package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    List<Category> findByParentIdIsNullAndIsActiveTrue();

    List<Category> findByParentIdAndIsActiveTrue(UUID parentId);

    List<Category> findByPathStartingWith(String pathPrefix);

    Optional<Category> findBySlug(String slug);
}
