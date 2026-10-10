package com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaystackSubaccountRequest(@JsonProperty("business_name") String businessName,
                                        @JsonProperty("settlement_bank") String settlementBank,
                                        @JsonProperty("account_number") String accountNumber,
                                        @JsonProperty("percentage_charge") java.math.BigDecimal percentageCharge,
                                        @JsonProperty("settlement_schedule") String settlementSchedule) {}
