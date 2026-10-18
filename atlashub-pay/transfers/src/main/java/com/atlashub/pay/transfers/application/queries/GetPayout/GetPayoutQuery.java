package com.atlashub.pay.transfers.application.queries.GetPayout;

public record GetPayoutQuery(
        Long organizationId,
        Long payoutId
) {
}
