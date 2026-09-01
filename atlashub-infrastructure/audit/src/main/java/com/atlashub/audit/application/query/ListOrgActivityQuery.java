package com.atlashub.audit.application.query;

public record ListOrgActivityQuery(
    Long organizationId,
    int page,
    int size
) {}
