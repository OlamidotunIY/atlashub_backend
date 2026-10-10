package com.atlashub.pay.txquery.application.queries;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public final class TransactionResults {
    private TransactionResults() {}
    public record AccountEntryResult(Long accountId,String accountType,String accountName,String entryType,
                                     String direction,BigDecimal amount,String currency){}
    public record TransactionResult(Long id,String type,String status,BigDecimal amount,BigDecimal fee,
            BigDecimal netAmount,String currency,String direction,String channel,String provider,String reference,
            String sourceSystem,String sourceReferenceId,String partyType,String partyReferenceId,Long outletId,
            String recipientName,String description,Map<String,String> metadata,List<AccountEntryResult> accountEntries,
            ZonedDateTime createdAt,ZonedDateTime completedAt){}
    public record VolumeResult(Long organizationId,String month,long chargeCount,BigDecimal chargeAmount,
                               long payoutCount,BigDecimal payoutAmount,String currency){}
}
