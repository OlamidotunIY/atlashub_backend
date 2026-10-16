package com.atlashub.pay.controls.application.commands.ApplyPaymentControlDecision;

public record ApplyPaymentControlDecisionCommand(
        Long decisionId,
        String providerReference
) {}
