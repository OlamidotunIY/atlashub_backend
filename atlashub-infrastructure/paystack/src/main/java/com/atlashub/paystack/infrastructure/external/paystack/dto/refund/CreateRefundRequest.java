package com.atlashub.paystack.infrastructure.external.paystack.dto.refund;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreateRefundRequest(
        String transaction,
        long amount,
        String currency,
        @JsonProperty("customer_note") String customerNote,
        @JsonProperty("merchant_note") String merchantNote
) {
}
