package com.atlashub.pay.txquery.application.queries.ListTransactions;

import com.atlashub.pay.txquery.domain.valueobject.TransactionDirection;import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;import com.atlashub.pay.txquery.domain.valueobject.TransactionType;import com.atlashub.shared.application.security.ApiEnvironment;import java.math.BigDecimal;import java.time.ZonedDateTime;

public record ListTransactionsQuery(Long organizationId,ApiEnvironment environment,Long accountId,TransactionType type,
        TransactionStatus status,TransactionDirection direction,String channel,String provider,String sourceSystem,
        String sourceReferenceId,String partyType,String partyReferenceId,Long outletId,String currency,String reference,
        String search,BigDecimal minAmount,BigDecimal maxAmount,ZonedDateTime from,ZonedDateTime to,int page,int size,
        String sortDirection){}
