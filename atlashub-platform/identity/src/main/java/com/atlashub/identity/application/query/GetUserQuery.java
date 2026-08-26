package com.atlashub.identity.application.query;


public record GetUserQuery(
    Long OrganizationId,
    Long UserId
) {}
