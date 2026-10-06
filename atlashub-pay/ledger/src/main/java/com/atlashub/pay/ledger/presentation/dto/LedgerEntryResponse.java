package com.atlashub.pay.ledger.presentation.dto;

import com.atlashub.pay.ledger.domain.valueobject.EntryType;

import java.math.BigDecimal;

public record LedgerEntryResponse(Long accountId, EntryType entryType, BigDecimal amount) {}
