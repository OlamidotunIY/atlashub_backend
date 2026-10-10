package com.atlashub.commerce.catalog.application.commands.CreateVendor;

import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;

import java.math.BigDecimal;

public record CreateVendorCommand(
        Long organizationId,
        Long userId,
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
