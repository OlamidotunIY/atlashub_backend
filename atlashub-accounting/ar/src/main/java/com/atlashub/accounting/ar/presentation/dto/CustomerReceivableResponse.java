package com.atlashub.accounting.ar.presentation.dto;

import com.atlashub.accounting.ar.domain.valueobject.ArStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public record CustomerReceivableResponse(
        Long id,
        Long organizationId,
        Long customerId,
        Long salesOrderId,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingBalance,
        String currency,
        LocalDate dueDate,
        ArStatus status,
        ZonedDateTime createdAt
) {}
