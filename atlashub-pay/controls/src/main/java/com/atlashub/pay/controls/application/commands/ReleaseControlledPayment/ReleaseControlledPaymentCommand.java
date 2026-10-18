package com.atlashub.pay.controls.application.commands.ReleaseControlledPayment;

public record ReleaseControlledPaymentCommand(
        Long decisionId,
        String reason
) {}
