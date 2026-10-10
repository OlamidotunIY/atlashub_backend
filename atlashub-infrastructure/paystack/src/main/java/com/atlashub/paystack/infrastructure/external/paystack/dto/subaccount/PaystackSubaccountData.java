package com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackSubaccountData(@JsonProperty("subaccount_code") String subaccountCode,
                                     @JsonProperty("business_name") String businessName,
                                     @JsonProperty("account_number") String accountNumber,
                                     @JsonProperty("settlement_bank") String settlementBank,
                                     @JsonProperty("settlement_schedule") String settlementSchedule,
                                     boolean active) {}
