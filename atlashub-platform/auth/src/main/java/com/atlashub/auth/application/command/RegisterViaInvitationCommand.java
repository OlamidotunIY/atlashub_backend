package com.atlashub.auth.application.command;

public record RegisterViaInvitationCommand(
    String token,
    String firstName,
    String lastName,
    String password
) {}
