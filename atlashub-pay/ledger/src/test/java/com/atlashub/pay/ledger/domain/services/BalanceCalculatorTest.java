package com.atlashub.pay.ledger.domain.services;

import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountStatus;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BalanceCalculatorTest {
    private final BalanceCalculator calculator = new BalanceCalculator();

    @Test
    void excludes_pending_settlement_from_operating_availability() {
        assertEquals(new BigDecimal("8000"), calculator.calculateAvailableBalance(
                LedgerAccountType.OPERATING, LedgerAccountStatus.ACTIVE,
                new BigDecimal("9000"), new BigDecimal("1000")));
    }

    @Test
    void frozen_account_has_no_available_balance() {
        assertEquals(BigDecimal.ZERO, calculator.calculateAvailableBalance(
                LedgerAccountType.PAYROLL_RESERVE, LedgerAccountStatus.FROZEN,
                new BigDecimal("9000"), BigDecimal.ZERO));
    }
}
