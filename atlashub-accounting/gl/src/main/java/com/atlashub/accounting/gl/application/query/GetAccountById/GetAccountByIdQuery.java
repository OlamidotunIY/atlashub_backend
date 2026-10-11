package com.atlashub.accounting.gl.application.query.GetAccountById;

public record GetAccountByIdQuery(
        Long accountId,
        Long organizationId
) {}
