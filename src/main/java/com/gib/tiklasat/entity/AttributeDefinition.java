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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Kategorilere özgü ilan özelliklerini tanımlayan varlık sınıfı.
 * (Örn: Araç kategorisi için vites tipi, renk, motor gücü gibi)
 * Hangi veri tipinde olacağı, birimi ve kısıtlamaları burada belirlenir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attribute_definitions")
public class AttributeDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attr_def_category"))
    private Category category;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "label", length = 80, nullable = false)
    private String label;

    @Column(name = "data_type", length = 16, nullable = false)
    private String dataType;

    @Column(name = "unit", length = 16)
    private String unit;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "is_filterable", nullable = false)
    private Boolean isFilterable = true;

    @Column(name = "min_value", precision = 18, scale = 4)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 18, scale = 4)
    private BigDecimal maxValue;

    @Column(name = "max_length")
    private Integer maxLength;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
