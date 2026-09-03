package com.atlashub.catalog.domain.model;

import com.atlashub.catalog.domain.events.HubProductCreatedEvent;
import com.atlashub.catalog.domain.events.HubProductDeactivatedEvent;
import com.atlashub.catalog.domain.events.HubProductUpdatedEvent;
import com.atlashub.catalog.domain.exception.CatalogErrorCode;
import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;
import com.atlashub.shared.domain.AggregateRoot;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class HubProduct extends AggregateRoot<Long> {
    private final Long id;
    private final ProductKey key;
    private String name;
    private String description;
    private ProductStatus status;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public HubProduct(Long id, ProductKey key, String name, String description, ProductStatus status, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.key = key;
        this.name = name;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static HubProduct create(Long id, ProductKey key, String name, String description) {
        HubProduct product = new HubProduct(id, key, name, description, ProductStatus.ACTIVE, ZonedDateTime.now(), ZonedDateTime.now());

        product.registerEvent(
                new HubProductCreatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        new HubProductCreatedEvent.Payload(key)
                )
        );

        return product;
    }

    public void updateDetails(String name, String description) {
        this.name = name;
        this.description = description;
        this.updatedAt = ZonedDateTime.now();

        this.registerEvent(
                new HubProductUpdatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        null
                )
        );
    }

    public void deactivate() {
        if (status != ProductStatus.ACTIVE) {
            throw new BusinessRuleException(CatalogErrorCode.PRODUCT_NOT_ACTIVE, "This product cannot be deactivated, cause it is not active");
        }

        this.status = ProductStatus.INACTIVE;

        this.registerEvent(
                new HubProductDeactivatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(id),
                        ZonedDateTime.now(),
                        null
                )
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
