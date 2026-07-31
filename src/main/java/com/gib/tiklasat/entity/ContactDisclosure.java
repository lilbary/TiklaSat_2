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

import java.time.Instant;
import java.util.UUID;

/**
 * İletişim bilgisi ifşalarını tutan varlık sınıfı.
 * Müzayede sonrası hangi kullanıcının iletişim bilgilerinin görüntülendiğini kaydeder.
 */
@Entity
@Table(name = "contact_disclosures")
@Getter
@Setter
@NoArgsConstructor
public class ContactDisclosure {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contact_disclosures_auction"))
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viewer_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contact_disclosures_viewer"))
    private User viewerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_contact_disclosures_subject"))
    private User subjectUser;

    @Column(name = "disclosed_fields", length = 100, nullable = false)
    private String disclosedFields;

    @Column(name = "viewer_ip", columnDefinition = "INET")
    private String viewerIp;

    @CreationTimestamp
    @Column(name = "disclosed_at", nullable = false, updatable = false)
    private Instant disclosedAt;
}
