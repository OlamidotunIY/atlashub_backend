package com.atlashub.identity.application.command;

import com.atlashub.identity.domain.model.GovernmentIdType;
import java.time.LocalDate;

public record CompleteComplianceOwnerCommand(
    Long merchantId,
    String ownerBvn,
    String ownerNin,
    LocalDate ownerDateOfBirth,
    String ownerAddress,
    GovernmentIdType ownerIdType,
    String ownerIdNumber,
    String rcNumber
) {}
