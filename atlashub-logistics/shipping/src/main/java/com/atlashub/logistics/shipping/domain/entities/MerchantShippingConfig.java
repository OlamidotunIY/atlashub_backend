package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.shared.domain.entities.AggregateRoot;
import java.time.ZonedDateTime;
import lombok.Getter;

@Getter
public class MerchantShippingConfig extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private boolean urgentDeliveryEnabled;
    private Address defaultPickupAddress;
    private ZonedDateTime updatedAt;

    public MerchantShippingConfig(
            Long id,
            Long organizationId,
            boolean urgentDeliveryEnabled,
            Address defaultPickupAddress,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.urgentDeliveryEnabled = urgentDeliveryEnabled;
        this.defaultPickupAddress = defaultPickupAddress;
        this.updatedAt = updatedAt;
    }

    public static MerchantShippingConfig create(
            Long id,
            Long organizationId,
            boolean urgentDeliveryEnabled,
            Address defaultPickupAddress
    ) {
        return new MerchantShippingConfig(
                id,
                organizationId,
                urgentDeliveryEnabled,
                defaultPickupAddress,
                ZonedDateTime.now()
        );
    }

    public void update(boolean urgentDeliveryEnabled, Address defaultPickupAddress, ZonedDateTime now) {
        this.urgentDeliveryEnabled = urgentDeliveryEnabled;
        if (defaultPickupAddress != null) {
            this.defaultPickupAddress = defaultPickupAddress;
        }
        this.updatedAt = now;
    }

    @Override
    public Long getId() {
        return id;
    }
}
