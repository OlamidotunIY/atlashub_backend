package com.atlashub.pay.controls.domain.valueobject;

import com.atlashub.shared.domain.valueobject.Money;

public record WithdrawalRuleSet(
        Money maxSingleWithdrawal,
        Money dailyWithdrawalLimit,
        boolean requiresApproval
) {}
