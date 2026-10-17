package com.atlashub.logistics.shipping.presentation.dto;

import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.shared.domain.valueobject.Money;
import jakarta.validation.constraints.NotNull;

public record DispatchBatchRequest(
        @NotNull CarrierChannel channel,
        String carrierCode,
        @NotNull Money actualBatchCost
) {}
