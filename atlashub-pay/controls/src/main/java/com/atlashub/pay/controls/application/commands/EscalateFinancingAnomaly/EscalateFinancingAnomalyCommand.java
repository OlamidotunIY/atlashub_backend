package com.atlashub.pay.controls.application.commands.EscalateFinancingAnomaly;

public record EscalateFinancingAnomalyCommand(
        Long facilityId,
        Long organizationId,
        String anomalyType,
        String severity,
        Long riskSignalId
) {}
