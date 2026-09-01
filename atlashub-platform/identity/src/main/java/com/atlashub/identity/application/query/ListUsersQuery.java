package com.atlashub.identity.application.query;


public record ListUsersQuery(
    Long OrganizationId,
    int page,
    int size,
    String emailFilter
) {}
