package com.atlashub.identity.application.command;

public record CreateUserCommand(
    String firstName,
    String lastName,
    String email,
    String phone,
    String country
) {}
