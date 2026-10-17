package com.atlashub.logistics.shipping.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record CompleteDeliveryRequest(
        @NotBlank String deliveryCode,
        @NotBlank String proofOfDeliveryUrl
) {}
