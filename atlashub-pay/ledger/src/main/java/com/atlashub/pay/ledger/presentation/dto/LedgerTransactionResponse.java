package com.atlashub.pay.ledger.presentation.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record LedgerTransactionResponse(
        Long transactionId, String reference, String sourceSystem, String sourceReferenceId,
        String description, String currency, ZonedDateTime postedAt, List<LedgerEntryResponse> entries) {}
