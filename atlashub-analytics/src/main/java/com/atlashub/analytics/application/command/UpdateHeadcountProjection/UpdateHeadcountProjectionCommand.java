package com.atlashub.analytics.application.command.UpdateHeadcountProjection;

public record UpdateHeadcountProjectionCommand(
        Long organizationId,
        String environment,
        String changeType
) {}
