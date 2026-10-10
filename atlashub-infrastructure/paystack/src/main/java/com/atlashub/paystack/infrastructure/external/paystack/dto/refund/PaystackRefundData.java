package com.atlashub.paystack.infrastructure.external.paystack.dto.refund;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackRefundData(
        Long id,
        String status,
        long amount,
        String currency,
        @JsonProperty("refund_reference") String refundReference
) {
    public String resolvedReference() {
        return refundReference == null || refundReference.isBlank() ? String.valueOf(id) : refundReference;
    }
}
