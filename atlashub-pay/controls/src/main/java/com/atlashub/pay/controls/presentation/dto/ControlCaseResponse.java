package com.atlashub.pay.controls.presentation.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record ControlCaseResponse(
        Long caseId,
        Long decisionId,
        Long organizationId,
        String status,
        List<String> submittedEvidenceRefs,
        ZonedDateTime openedAt,
        Long resolvedBy,
        ZonedDateTime resolvedAt,
        String resolutionReason
) {}
