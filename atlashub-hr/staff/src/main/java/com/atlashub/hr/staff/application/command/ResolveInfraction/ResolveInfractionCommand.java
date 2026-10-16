package com.atlashub.hr.staff.application.command.ResolveInfraction;

public record ResolveInfractionCommand(
        Long infractionId,
        String resolutionNotes
) {}
