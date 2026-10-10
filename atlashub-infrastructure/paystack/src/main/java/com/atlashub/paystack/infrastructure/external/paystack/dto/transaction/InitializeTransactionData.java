package com.atlashub.paystack.infrastructure.external.paystack.dto.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;

public record InitializeTransactionData(@JsonProperty("authorization_url") String authorizationUrl,
                                        @JsonProperty("access_code") String accessCode, String reference) {
}
