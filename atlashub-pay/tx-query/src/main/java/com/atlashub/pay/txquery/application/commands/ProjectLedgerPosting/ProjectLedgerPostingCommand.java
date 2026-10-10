package com.atlashub.pay.txquery.application.commands.ProjectLedgerPosting;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record ProjectLedgerPostingCommand(Long organizationId, String environment, String reference,
                                          String sourceSystem, String sourceReferenceId, String description,
                                          String currency, ZonedDateTime postedAt, List<Entry> entries) {
    public record Entry(Long accountId, String entryType, BigDecimal amount) {
    }
}
