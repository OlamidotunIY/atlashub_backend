package com.atlashub.pay.settlement.application.commands.RecordSettlement;

import com.atlashub.pay.settlement.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;
import java.util.List;

public record RecordSettlementCommand(
        Long organizationId,
        ApiEnvironment environment,
        PaymentProvider provider,
        String providerSettlementId,
        String providerSubaccountCode,
        Long anchorDepositAccountId,
        Money grossAmount,
        Money netAmount,
        Money providerFeeAmount,
        ZonedDateTime providerSettledAt,
        List<String> transactionReferences
) {
}
