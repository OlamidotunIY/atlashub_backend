package com.atlashub.pay.controls.infrastructure.services;

import com.atlashub.shared.application.port.ControlledLedgerPort;
import com.atlashub.shared.domain.valueobject.Money;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.UUID;

@Component
public class ControlledLedgerAdapter implements ControlledLedgerPort {

    @Override
    public LedgerControlResult freezeLedgerBalance(Long organizationId, Long facilityId, Money amount, String reference) {
        String freezeRef = "LEDGER-FRZ-" + UUID.randomUUID().toString().substring(0, 8);
        return new LedgerControlResult(true, freezeRef, null, ZonedDateTime.now());
    }

    @Override
    public LedgerControlResult releaseLedgerBalance(Long organizationId, Long facilityId, Money amount, String reference) {
        return new LedgerControlResult(true, reference, null, ZonedDateTime.now());
    }
}
