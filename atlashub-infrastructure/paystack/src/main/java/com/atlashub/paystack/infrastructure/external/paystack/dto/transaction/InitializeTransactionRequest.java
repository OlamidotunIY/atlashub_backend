package com.atlashub.paystack.infrastructure.external.paystack.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record InitializeTransactionRequest(String email, long amount, String currency, String reference,
                                           List<String> channels, String subaccount, String bearer,
                                           @JsonProperty("callback_url") String callbackUrl,
                                           Map<String, Object> metadata) {
}
