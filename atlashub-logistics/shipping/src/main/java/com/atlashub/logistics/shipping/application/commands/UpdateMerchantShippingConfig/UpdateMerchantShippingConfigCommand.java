package com.atlashub.logistics.shipping.application.commands.UpdateMerchantShippingConfig;

import com.atlashub.logistics.shipping.domain.valueobject.Address;

public record UpdateMerchantShippingConfigCommand(
        Long organizationId,
        boolean urgentDeliveryEnabled,
        Address defaultPickupAddress
) {}
