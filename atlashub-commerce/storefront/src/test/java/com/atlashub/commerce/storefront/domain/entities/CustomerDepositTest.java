package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.DepositStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerDepositTest {

    @Test
    @DisplayName("Should create deposit in ACTIVE status")
    void create_shouldInitializeActiveDeposit() {
        Money deposit = Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN);
        Money total = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerDeposit customerDeposit = CustomerDeposit.create(1L, 99L, deposit, total);

        assertEquals(1L, customerDeposit.getId());
        assertEquals(99L, customerDeposit.getSalesOrderId());
        assertEquals(DepositStatus.ACTIVE, customerDeposit.getStatus());
        assertEquals(0, customerDeposit.getAmountPaid().amount().compareTo(new BigDecimal("20000.00")));
        assertEquals(0, customerDeposit.getBalanceRemaining().amount().compareTo(new BigDecimal("80000.00")));
    }

    @Test
    @DisplayName("Should create deposit with status FULFILLED if initial deposit covers total")
    void create_shouldInitializeFulfilledDeposit_whenFullyPaid() {
        Money total = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerDeposit customerDeposit = CustomerDeposit.create(1L, 99L, total, total);

        assertEquals(DepositStatus.FULFILLED, customerDeposit.getStatus());
        assertEquals(0, customerDeposit.getBalanceRemaining().amount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should add payment and fulfill when balance reaches zero")
    void addPayment_shouldReduceBalanceAndFulfill() {
        Money deposit = Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN);
        Money total = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerDeposit customerDeposit = CustomerDeposit.create(1L, 99L, deposit, total);

        customerDeposit.addPayment(Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN));

        assertEquals(DepositStatus.FULFILLED, customerDeposit.getStatus());
        assertEquals(0, customerDeposit.getAmountPaid().amount().compareTo(new BigDecimal("50000.00")));
        assertEquals(0, customerDeposit.getBalanceRemaining().amount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should recall deposit and update status to RECALLED")
    void recall_shouldTransitionToRecalled() {
        Money deposit = Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN);
        Money total = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerDeposit customerDeposit = CustomerDeposit.create(1L, 99L, deposit, total);

        customerDeposit.recall();

        assertEquals(DepositStatus.RECALLED, customerDeposit.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when mutating non-ACTIVE deposit")
    void addPayment_shouldThrowException_whenNotActive() {
        Money deposit = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerDeposit customerDeposit = CustomerDeposit.create(1L, 99L, deposit, deposit);

        assertThrows(InvalidOrderStateException.class, () ->
                customerDeposit.addPayment(Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN))
        );
        assertThrows(InvalidOrderStateException.class, customerDeposit::recall);
    }
}
