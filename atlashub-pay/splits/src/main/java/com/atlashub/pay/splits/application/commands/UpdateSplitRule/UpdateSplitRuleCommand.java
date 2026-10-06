package com.atlashub.pay.splits.application.commands.UpdateSplitRule;

public record UpdateSplitRuleCommand(
        Long splitRuleId,
        Long organizationId,
        String name,
        com.atlashub.pay.splits.domain.valueobject.SplitType type,
        java.math.BigDecimal platformFeePercentage,
        java.util.List<com.atlashub.pay.splits.application.commands.CreateSplitRule.CreateSplitRuleCommand.SplitSubaccountItem> subaccounts
) {
}

