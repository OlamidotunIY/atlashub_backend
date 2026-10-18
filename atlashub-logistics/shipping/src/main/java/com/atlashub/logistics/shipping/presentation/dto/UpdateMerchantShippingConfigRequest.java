package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.Address;

public record UpdateMerchantShippingConfigRequest(
        boolean urgentDeliveryEnabled,
        Address defaultPickupAddress
) {}
