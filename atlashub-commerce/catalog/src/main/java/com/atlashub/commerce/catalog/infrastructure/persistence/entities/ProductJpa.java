package com.atlashub.commerce.catalog.infrastructure.persistence.entities;

import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "commerce_products",
        indexes = {
                @Index(name = "Idx_comm_prod_org_id", columnList = "organization_id"),
                @Index(name = "Idx_comm_prod_org_code", columnList = "organization_id, code", unique = true),
                @Index(name = "Idx_comm_prod_org_status", columnList = "organization_id, status"),
                @Index(name = "Idx_comm_prod_vendor_id", columnList = "vendor_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "manufacturer_id")
    private Long manufacturerId;

    @Column(name = "is_taxable", nullable = false)
    private boolean taxable;

    @Column(name = "is_service", nullable = false)
    private boolean service;

    @Column(name = "has_variants", nullable = false)
    private boolean hasVariants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProductType type;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Version
    private Long version;
}
