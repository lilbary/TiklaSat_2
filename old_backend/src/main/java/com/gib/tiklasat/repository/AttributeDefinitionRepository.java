package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.AttributeDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinition, UUID> {

    List<AttributeDefinition> findByCategoryIdAndIsActiveTrue(UUID categoryId);
}
