package com.atlashub.iam.application.commands.InviteMember;

public record InviteMemberCommand(
    Long orgId,
    Long invitedByUserId,
    String email,
    Long customRoleId
) {}
