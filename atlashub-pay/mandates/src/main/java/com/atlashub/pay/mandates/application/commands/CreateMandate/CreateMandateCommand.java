package com.atlashub.pay.mandates.application.commands.CreateMandate;

import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMandateCommand(
        Long organizationId,
        String customerId,
        String email,
        BigDecimal amount,
        String currency,
        MandateFrequency frequency,
        String authorizationCode,
        LocalDate firstChargeDate
) {}
