package com.atlashub.pay.controls.application.commands.EvaluatePaymentInstruction;

import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.domain.valueobject.Money;

public record EvaluatePaymentInstructionCommand(
        Long facilityId,
        Long organizationId,
        String paymentInstructionReference,
        PaymentRail paymentRail,
        PaymentContext paymentContext,
        String categoryCode,
        String beneficiary,
        Money amount,
        Long riskSignalId
) {}
