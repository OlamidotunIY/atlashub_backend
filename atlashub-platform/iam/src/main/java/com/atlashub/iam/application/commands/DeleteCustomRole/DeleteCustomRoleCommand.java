package com.atlashub.iam.application.commands.DeleteCustomRole;

public record DeleteCustomRoleCommand(Long roleId, Long requestedByUserId) {}
