package com.atlashub.pay.splits.application.commands.AddSplitSubaccount;

public record AddSplitSubaccountCommand(
        Long splitRuleId,
        Long organizationId,
        com.atlashub.pay.splits.domain.valueobject.RecipientType recipientType,
        String recipientId,
        java.math.BigDecimal share,
        String description
) {
}

