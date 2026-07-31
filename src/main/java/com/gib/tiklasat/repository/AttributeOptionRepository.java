package com.gib.tiklasat.repository;

import com.gib.tiklasat.entity.AttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttributeOptionRepository extends JpaRepository<AttributeOption, UUID> {

    List<AttributeOption> findByAttributeDefinitionIdAndIsActiveTrue(UUID attributeDefinitionId);
}
