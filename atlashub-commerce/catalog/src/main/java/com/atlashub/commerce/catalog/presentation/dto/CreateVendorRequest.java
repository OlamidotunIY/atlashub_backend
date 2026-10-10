package com.atlashub.commerce.catalog.presentation.dto;

import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateVendorRequest(
        @NotNull(message = "User ID is required")
        Long userId,
        @NotBlank(message = "Business name is required")
        String businessName,
        String email,
        String phone,
        String settlementBankCode,
        String settlementAccountNumber,
        String settlementAccountName,
        BigDecimal commissionRate,
        DisbursementSchedule disbursementSchedule
) {
}
