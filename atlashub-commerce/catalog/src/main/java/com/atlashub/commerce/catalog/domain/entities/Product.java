package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.events.ProductApprovedEvent;
import com.atlashub.commerce.catalog.domain.events.ProductCreatedEvent;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.ProductStatus;
import com.atlashub.commerce.catalog.domain.valueobject.ProductType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Product extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long vendorId;
    private final String code;
    private String name;
    private String description;
    private Long categoryId;
    private Long departmentId;
    private Long manufacturerId;
    private boolean taxable;
    private final boolean service;
    private boolean hasVariants;
    private ProductStatus status;
    private final ProductType type;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public Product(Long id, Long organizationId, Long vendorId, String code, String name,
                   String description, Long categoryId, Long departmentId, Long manufacturerId,
                   boolean taxable, boolean service, boolean hasVariants, ProductStatus status,
                   ProductType type, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.vendorId = vendorId;
        this.code = code;
        this.name = name;
        this.description = description;
        this.categoryId = categoryId;
        this.departmentId = departmentId;
        this.manufacturerId = manufacturerId;
        this.taxable = taxable;
        this.service = service;
        this.hasVariants = hasVariants;
        this.status = status;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static Product create(Long id, Long organizationId, Long vendorId, String code, String name,
                                 String description, Long categoryId, Long departmentId, Long manufacturerId,
                                 boolean taxable, boolean service, boolean hasVariants, ProductType type) {
        ZonedDateTime now = ZonedDateTime.now();
        ProductStatus initialStatus = vendorId != null ? ProductStatus.PENDING_APPROVAL : ProductStatus.ACTIVE;
        Product product = new Product(id, organizationId, vendorId, code, name, description, categoryId,
                departmentId, manufacturerId, taxable, service, hasVariants, initialStatus, type, now, now);

        product.registerEvent(new ProductCreatedEvent(
                UUID.randomUUID().toString(),
                id,
                now,
                CorrelationId.getOrCreate(),
                new ProductCreatedEvent.Payload(organizationId, vendorId, code, name, type, service, now)
        ));
        return product;
    }

    public void approve() {
        if (this.status == ProductStatus.ACTIVE) {
            return;
        }
        if (this.status != ProductStatus.PENDING_APPROVAL) {
            throw new InvalidProductStateException("Only pending approval products can be approved");
        }
        this.status = ProductStatus.ACTIVE;
        touch();
        registerEvent(new ProductApprovedEvent(
                UUID.randomUUID().toString(),
                this.id,
                this.updatedAt,
                CorrelationId.getOrCreate(),
                new ProductApprovedEvent.Payload(this.organizationId, this.vendorId, this.code, this.name, this.updatedAt)
        ));
    }

    public void reject(String reason) {
        if (this.status != ProductStatus.PENDING_APPROVAL) {
            throw new InvalidProductStateException("Only pending approval products can be rejected");
        }
        this.status = ProductStatus.INACTIVE;
        if (reason != null && !reason.isBlank()) {
            this.description = this.description != null && !this.description.isBlank() ?
                    this.description + " [Rejection reason: " + reason.trim() + "]" : "Rejection: " + reason.trim();
        }
        touch();
    }

    public void deactivate() {
        if (this.status == ProductStatus.INACTIVE) {
            return;
        }
        if (this.status != ProductStatus.ACTIVE) {
            throw new InvalidProductStateException("Only active products can be deactivated");
        }
        this.status = ProductStatus.INACTIVE;
        touch();
    }

    public void activate() {
        if (this.status == ProductStatus.ACTIVE) {
            return;
        }
        if (this.status != ProductStatus.INACTIVE) {
            throw new InvalidProductStateException("Only inactive products can be activated");
        }
        this.status = ProductStatus.ACTIVE;
        touch();
    }

    public void markAsDeleted() {
        this.status = ProductStatus.DELETED;
        touch();
    }

    public void updateDetails(String name, String description, Long categoryId,
                              Long departmentId, Long manufacturerId, boolean taxable, boolean hasVariants) {
        if (this.status == ProductStatus.DELETED) {
            throw new InvalidProductStateException("Cannot update a deleted product");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductStateException("Product name is required");
        }
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.categoryId = categoryId;
        this.departmentId = departmentId;
        this.manufacturerId = manufacturerId;
        this.taxable = taxable;
        this.hasVariants = hasVariants;
        touch();
    }

    public boolean isPendingApproval() {
        return this.status == ProductStatus.PENDING_APPROVAL;
    }

    public boolean isActive() {
        return this.status == ProductStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == ProductStatus.DELETED;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Product id cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidProductStateException("Organization id cannot be null");
        }
        if (code == null || code.isBlank()) {
            throw new InvalidProductStateException("Product code is required");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductStateException("Product name is required");
        }
        if (status == null) {
            throw new InvalidProductStateException("Product status cannot be null");
        }
        if (type == null) {
            throw new InvalidProductStateException("Product type cannot be null");
        }
        if (createdAt == null || updatedAt == null) {
            throw new InvalidProductStateException("Timestamps cannot be null");
        }
    }

    private void touch() {
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
