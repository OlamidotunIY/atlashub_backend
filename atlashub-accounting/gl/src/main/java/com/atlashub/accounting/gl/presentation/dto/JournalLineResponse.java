package com.atlashub.accounting.gl.presentation.dto;

import com.atlashub.accounting.gl.domain.valueobject.EntryType;

import java.math.BigDecimal;

public record JournalLineResponse(
        Long id,
        Long accountId,
        BigDecimal amount,
        String currency,
        EntryType type
) {}
