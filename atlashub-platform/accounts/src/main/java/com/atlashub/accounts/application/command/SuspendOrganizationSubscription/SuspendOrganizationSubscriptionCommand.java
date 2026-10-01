package com.atlashub.accounts.application.command.SuspendOrganizationSubscription;

public record SuspendOrganizationSubscriptionCommand(Long organizationId, String reason) {}
