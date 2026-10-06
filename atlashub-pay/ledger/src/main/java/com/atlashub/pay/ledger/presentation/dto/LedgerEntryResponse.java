package com.atlashub.pay.ledger.presentation.dto;

import java.math.BigDecimal;

public record LedgerEntryResponse(Long accountId, String entryType, BigDecimal amount) {}
