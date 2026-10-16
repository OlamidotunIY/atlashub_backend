package com.atlashub.pay.controls.application.queries.ListControlDecisions;

public record ListControlDecisionsQuery(
        Long organizationId,
        int pageNumber,
        int pageSize
) {}
