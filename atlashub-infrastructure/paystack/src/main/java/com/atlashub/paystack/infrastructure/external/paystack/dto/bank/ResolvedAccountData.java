package com.atlashub.paystack.infrastructure.external.paystack.dto.bank;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResolvedAccountData(@JsonProperty("account_number") String accountNumber,
                                  @JsonProperty("account_name") String accountName) {}
