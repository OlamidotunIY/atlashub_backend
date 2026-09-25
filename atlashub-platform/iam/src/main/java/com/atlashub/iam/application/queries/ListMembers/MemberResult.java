package com.atlashub.iam.application.queries.ListMembers;

import java.time.ZonedDateTime;

public record MemberResult(
    Long id,
    Long organizationId,
    Long userId,
    Long customRoleId,
    String status,
    ZonedDateTime joinedAt,
    Long invitedBy,
    ZonedDateTime updatedAt
) {
}
