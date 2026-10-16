package com.atlashub.hr.staff.application.command.LogInfraction;

import com.atlashub.hr.staff.domain.valueobject.InfractionSeverity;

public record LogInfractionCommand(
        Long employeeId,
        Long organizationId,
        String description,
        InfractionSeverity severity,
        Long reportedBy
) {}
