package com.atlashub.commerce.catalog.application.queries.ListVendors;

import com.atlashub.commerce.catalog.domain.valueobject.DisbursementSchedule;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record VendorResult(
        Long id,
        Long organizationId,
        Long userId,
        String businessName,
        String email,
        String phone,
        String settlementBankCode,
        String settlementAccountNumber,
        String settlementAccountName,
        BigDecimal commissionRate,
        DisbursementSchedule disbursementSchedule,
        VendorStatus status,
        ZonedDateTime createdAt
) {
}
