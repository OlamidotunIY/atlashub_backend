package com.atlashub.authentication.application.query.GetUserInfo;

public record GetUserInfoResult(
        String sub,
        String email,
        boolean emailVerified,
        String name,
        String givenName,
        String familyName,
        Long activeOrganizationId,
        String environment
) {
}
