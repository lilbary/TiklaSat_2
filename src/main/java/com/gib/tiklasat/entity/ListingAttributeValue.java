package com.gib.tiklasat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * İlan Nitelik Değeri (Listing Attribute Value) varlığı.
 * Kategoriye özel dinamik özelliklerin (EAV modeli) ilan bazındaki değerlerini tutar.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "listing_attribute_values")
public class ListingAttributeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lav_listing_id"))
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_definition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lav_attribute_definition_id"))
    private AttributeDefinition attributeDefinition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", foreignKey = @ForeignKey(name = "fk_lav_option_id"))
    private AttributeOption option;

    @Column(name = "value_text", columnDefinition = "TEXT")
    private String valueText;

    @Column(name = "value_number", precision = 18, scale = 4)
    private BigDecimal valueNumber;

    @Column(name = "value_bool")
    private Boolean valueBool;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
