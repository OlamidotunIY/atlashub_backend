package com.atlashub.pay.splits.application.commands.DeactivateSplitRule;

public record DeactivateSplitRuleCommand(
        Long splitRuleId,
        Long organizationId
) {
}

