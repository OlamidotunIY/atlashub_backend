package com.atlashub.iam.application.commands.AcceptInvitation;

public record AcceptInvitationCommand(String token, Long acceptingUserId) {}
