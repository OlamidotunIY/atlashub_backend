package com.atlashub.accounts.application.command.ApplyAuthenticationEvent;

import com.atlashub.shared.application.security.ApiEnvironment;

public record ApplyAuthenticationEventCommand(Long userId, Long activeOrganizationId,
                                              ApiEnvironment activeEnvironment, boolean emailVerified) {}
