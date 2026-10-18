package com.atlashub.shared.application.port;

import com.atlashub.shared.domain.valueobject.Money;
import java.time.ZonedDateTime;

public interface ControlledLedgerPort {

    record LedgerControlResult(
            boolean successful,
            String reference,
            String errorMessage,
            ZonedDateTime timestamp
    ) {
        public static LedgerControlResult success(String reference) {
            return new LedgerControlResult(true, reference, null, ZonedDateTime.now());
        }

        public static LedgerControlResult failure(String errorMessage) {
            return new LedgerControlResult(false, null, errorMessage, ZonedDateTime.now());
        }
    }

    LedgerControlResult freezeLedgerBalance(Long organizationId, Long facilityId, Money amount, String reference);

    LedgerControlResult releaseLedgerBalance(Long organizationId, Long facilityId, Money amount, String reference);
}
