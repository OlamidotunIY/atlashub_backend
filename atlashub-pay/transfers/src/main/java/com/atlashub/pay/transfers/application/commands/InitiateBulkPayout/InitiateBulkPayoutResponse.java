package com.atlashub.pay.transfers.application.commands.InitiateBulkPayout;

import java.util.List;

public record InitiateBulkPayoutResponse(
        String bulkReference,
        int totalCount,
        List<Long> payoutIds
) {
}
