package com.atlashub.identity.application.command;

public record AcceptInvitationCommand(
    String token,
    Long acceptingUserId
) {}
