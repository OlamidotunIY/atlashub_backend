package com.atlashub.pay.controls.application.queries.ListControlCases;

import java.time.ZonedDateTime;
import java.util.List;

public record ControlCaseResult(
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
