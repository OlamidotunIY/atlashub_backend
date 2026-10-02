package com.atlashub.accounts.application.command.ApplyAuthenticationEvent;

public record ApplyAuthenticationEventCommand(Long userId, Long activeOrganizationId, boolean emailVerified) {}
