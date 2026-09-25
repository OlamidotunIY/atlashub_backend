package com.atlashub.iam.application.queries.GetMemberDetails;

import java.time.ZonedDateTime;

public record MemberDetailsResult(
    Long id,
    Long organizationId,
    Long userId,
    Long customRoleId,
    String status,
    ZonedDateTime joinedAt,
    Long invitedBy,
    ZonedDateTime updatedAt
) {}
