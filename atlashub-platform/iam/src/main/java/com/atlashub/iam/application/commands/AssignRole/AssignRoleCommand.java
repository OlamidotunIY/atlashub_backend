package com.atlashub.iam.application.commands.AssignRole;

public record AssignRoleCommand(
    Long memberId,
    Long newRoleId,
    Long requestedByUserId
) {}
