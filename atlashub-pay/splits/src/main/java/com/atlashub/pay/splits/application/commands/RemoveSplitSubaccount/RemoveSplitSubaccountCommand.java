package com.atlashub.pay.splits.application.commands.RemoveSplitSubaccount;

public record RemoveSplitSubaccountCommand(
        Long splitRuleId,
        Long organizationId,
        Long subaccountId
) {
}

