package com.atlashub.identity.adapter.out.persistence.entity;

import com.atlashub.identity.domain.valueobject.BusinessSize;
import com.atlashub.identity.domain.valueobject.BusinessType;
import com.atlashub.identity.domain.valueobject.ComplianceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(name = "organizations",
    indexes = {
        @Index(name = "idx_organization_business_type", columnList = "business_type")
    }
)
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationJpaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Setter
    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_type", nullable = false)
    private BusinessType businessType;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_size", nullable = false)
    private BusinessSize businessSize;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "logo_url")
    private String logoUrl;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", nullable = false)
    private ComplianceStatus complianceStatus;

    @Setter
    @Column(name = "compliance_step")
    private String complianceStep;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
