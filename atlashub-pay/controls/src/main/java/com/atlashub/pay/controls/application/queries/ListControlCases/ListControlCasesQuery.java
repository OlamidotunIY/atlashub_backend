package com.atlashub.pay.controls.application.queries.ListControlCases;

public record ListControlCasesQuery(
        Long organizationId,
        int pageNumber,
        int pageSize
) {}
