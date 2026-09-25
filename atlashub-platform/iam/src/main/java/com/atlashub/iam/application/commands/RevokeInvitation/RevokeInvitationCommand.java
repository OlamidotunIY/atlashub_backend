package com.atlashub.iam.application.commands.RevokeInvitation;

public record RevokeInvitationCommand(Long invitationId, Long revokedByUserId) {}
