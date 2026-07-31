package com.gib.tiklasat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Belirli bir özelliğin alabileceği önceden tanımlanmış değerleri tutar (Enum/Seçimlik alanlar için).
 * (Örn: Vites Tipi özelliği için "Manuel", "Otomatik" seçenekleri)
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attribute_options")
public class AttributeOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attr_opt_definition"))
    private AttributeDefinition attributeDefinition;

    @Column(name = "value", length = 60, nullable = false)
    private String value;

    @Column(name = "label", length = 80, nullable = false)
    private String label;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
