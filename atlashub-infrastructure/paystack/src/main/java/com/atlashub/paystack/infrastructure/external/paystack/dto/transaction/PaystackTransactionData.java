package com.atlashub.paystack.infrastructure.external.paystack.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record PaystackTransactionData(Long id, String status, String reference, long amount, Long fees, String currency,
                                      String channel, @JsonProperty("paid_at") ZonedDateTime paidAt,
                                      @JsonProperty("subaccount") Object subaccount) {
}
