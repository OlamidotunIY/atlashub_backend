package com.atlashub.pay.txquery.domain.valueobject;

import com.atlashub.shared.application.security.ApiEnvironment;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record TransactionFilter(Long organizationId, ApiEnvironment environment, Long accountId, TransactionType type,
                                TransactionStatus status, TransactionDirection direction, String channel,
                                String provider, String sourceSystem, String sourceReferenceId, String partyType,
                                String partyReferenceId, Long outletId, String currency, String reference,
                                String search, BigDecimal minAmount, BigDecimal maxAmount, ZonedDateTime dateFrom,
                                ZonedDateTime dateTo, int page, int size, String sortDirection) {
}
