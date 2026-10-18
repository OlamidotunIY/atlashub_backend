package com.atlashub.pay.mandates.application.queries.ListMandates;

import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;

public record ListMandatesQuery(
        Long organizationId,
        String customerId,
        MandateStatus status
) {}
