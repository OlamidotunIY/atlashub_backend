package com.atlashub.pay.mandates.application.commands.CreateMandate;

import java.time.LocalDate;

public record CreateMandateResponse(
        Long mandateId,
        String status,
        LocalDate nextChargeDate
) {}
