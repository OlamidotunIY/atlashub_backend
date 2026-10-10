package com.atlashub.paystack.infrastructure.external.paystack.dto.settlement;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;

public record PaystackSettlementData(Long id, String status, long amount,
                                     @JsonProperty("total_amount") long totalAmount, long fees, String currency,
                                     @JsonProperty("settled_at") ZonedDateTime settledAt,
                                     @JsonProperty("settlement_date") ZonedDateTime settlementDate) {}
