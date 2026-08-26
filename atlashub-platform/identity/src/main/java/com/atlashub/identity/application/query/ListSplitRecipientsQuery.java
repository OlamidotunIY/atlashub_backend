package com.atlashub.identity.application.query;


public record ListSplitRecipientsQuery(
    Long OrganizationId,
    int page,
    int size
) {}
