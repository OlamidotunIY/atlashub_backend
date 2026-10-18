package com.atlashub.pay.controls.presentation.dto;

import java.time.ZonedDateTime;
import java.util.Set;

public record PaymentControlDecisionResponse(
        Long decisionId,
        Long policyId,
        Long facilityId,
        Long organizationId,
        String paymentInstructionReference,
        String paymentRail,
        String paymentContext,
        String evaluatedAction,
        String enforcedAction,
        Set<String> reasonCodes,
        Long riskSignalId,
        String providerControlReference,
        String status,
        ZonedDateTime evaluatedAt,
        ZonedDateTime appliedAt
) {}
