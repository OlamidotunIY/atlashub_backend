package com.atlashub.pay.splits.application.commands.CreateSplitRule;

import com.atlashub.pay.splits.domain.valueobject.RecipientType;
import com.atlashub.pay.splits.domain.valueobject.SplitType;

import java.math.BigDecimal;
import java.util.List;

public record CreateSplitRuleCommand(
        Long organizationId,
        String name,
        SplitType type,
        BigDecimal platformFeePercentage,
        List<SplitSubaccountItem> subaccounts
) {
    public record SplitSubaccountItem(
            RecipientType recipientType,
            String recipientId,
            BigDecimal share,
            String description
    ) {}
}
