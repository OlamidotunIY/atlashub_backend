package com.atlashub.pay.controls.domain.valueobject;

import com.atlashub.shared.domain.valueobject.Money;

public record AmountFrequencyRuleSet(
        Money maxTransactionAmount,
        Money dailyAmountLimit,
        Money monthlyAmountLimit,
        int maxDailyTransactions
) {}
