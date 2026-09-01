package com.atlashub.identity.application.query;


public record GetSplitRecipientQuery(
    Long OrganizationId,
    Long SplitRecipientId
) {}
