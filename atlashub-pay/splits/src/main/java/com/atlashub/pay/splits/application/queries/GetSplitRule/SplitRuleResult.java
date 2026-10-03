package com.atlashub.pay.splits.application.queries.GetSplitRule;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record SplitRuleResult(
        Long id,
        Long organizationId,
        String name,
        String type,
        BigDecimal platformFeePercentage,
        List<SplitSubaccountResult> subaccounts,
        boolean isActive,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public record SplitSubaccountResult(
            Long id,
            String recipientType,
            String recipientId,
            BigDecimal share,
            String description
    ) {}
}
