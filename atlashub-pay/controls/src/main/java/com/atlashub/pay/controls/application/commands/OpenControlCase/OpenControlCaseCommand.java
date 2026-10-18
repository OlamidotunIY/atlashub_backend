package com.atlashub.pay.controls.application.commands.OpenControlCase;

import com.atlashub.pay.controls.domain.valueobject.EvidenceRequirement;
import java.util.List;

public record OpenControlCaseCommand(
        Long decisionId,
        Long organizationId,
        List<EvidenceRequirement> requestedEvidence
) {}
