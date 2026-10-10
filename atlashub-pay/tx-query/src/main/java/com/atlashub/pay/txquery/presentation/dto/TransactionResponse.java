package com.atlashub.pay.txquery.presentation.dto;

import java.math.BigDecimal;import java.time.ZonedDateTime;import java.util.List;import java.util.Map;

public record TransactionResponse(Long id,String type,String status,BigDecimal amount,BigDecimal fee,
        BigDecimal netAmount,String currency,String direction,String channel,String provider,String reference,
        String sourceSystem,String sourceReferenceId,String partyType,String partyReferenceId,Long outletId,
        String recipientName,String description,Map<String,String> metadata,List<AccountEntryResponse> accountEntries,
        ZonedDateTime createdAt,ZonedDateTime completedAt){
    public record AccountEntryResponse(Long accountId,String accountType,String accountName,String entryType,
                                       String direction,BigDecimal amount,String currency){}
}
