package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.CategoryAttribute;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CategoryAttributeDto {
    private UUID id;
    private String name;
    private String label;
    private String fieldType;
    private List<String> options;
    private boolean isRequired;
    private String unit;
    private int sortOrder;

    public static CategoryAttributeDto fromEntity(CategoryAttribute entity) {
        CategoryAttributeDto dto = new CategoryAttributeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLabel(entity.getLabel());
        dto.setFieldType(entity.getFieldType());
        dto.setOptions(entity.getOptions());
        dto.setRequired(entity.isRequired());
        dto.setUnit(entity.getUnit());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }
}