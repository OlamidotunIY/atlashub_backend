package com.atlashub.iam.application.commands.SuspendOrganizationMembers;

public record SuspendOrganizationMembersCommand(Long organizationId, String reason) {}
