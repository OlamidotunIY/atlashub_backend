package com.atlashub.identity.application.command;

import java.util.Map;

public record CreateUserCommand(
    Long OrganizationId,
    String firstName,
    String lastName,
    String email,
    String phone,
    Map<String, String> metadata
) {}
