package com.atlashub.pay.ledger.presentation.dto;

import com.atlashub.pay.ledger.domain.valueobject.SourceSystem;

import java.time.ZonedDateTime;
import java.util.List;

public record LedgerTransactionResponse(
        Long transactionId, String reference, SourceSystem sourceSystem, String sourceReferenceId,
        String description, String currency, ZonedDateTime postedAt, List<LedgerEntryResponse> entries) {}
