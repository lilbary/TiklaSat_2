package com.gib.tiklasat.dto;

import com.gib.tiklasat.entity.Category;
import lombok.Data;
import java.util.UUID;

@Data
public class CategoryDto {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;

    public static CategoryDto fromEntity(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }
        return dto;
    }
}
