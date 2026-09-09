package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "category_attributes")
public class CategoryAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    //Bir kategorinin birden fazla özelliği olabilir ama her özellik tek bir kategoriye ait.
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "label", length = 100, nullable = false)
    private String label;

    @Column(name = "field_type", length = 20, nullable = false)
    private String fieldType;

    @Column(name = "options", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON) //jsonb icin
    private List<String> options;
    //Veritabanında options kolonu JSONB tipinde ["Benzin","Dizel","Elektrik"] tutuyor.
    //Bu anotasyon Hibernate'e "bunu Java List<String>'e çevir" diyor.

    @Column(name = "is_required", nullable = false)
    private boolean isRequired = true;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}