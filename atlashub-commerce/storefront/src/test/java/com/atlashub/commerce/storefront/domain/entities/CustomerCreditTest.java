package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.CreditLimitExceededException;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditBlockedException;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomerCreditTest {

    @Test
    @DisplayName("Should create customer credit account in SETTLED status")
    void create_shouldInitializeAccount() {
        Money limit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);

        assertEquals(1L, credit.getId());
        assertEquals(10L, credit.getOrganizationId());
        assertEquals(50L, credit.getCustomerId());
        assertEquals(CreditStatus.SETTLED, credit.getStatus());
        assertEquals(0, credit.getOutstandingDebt().amount().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Should extend credit within limit")
    void extendCredit_shouldIncreaseDebt() {
        Money limit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);

        credit.extendCredit(Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN));

        assertEquals(0, credit.getOutstandingDebt().amount().compareTo(new BigDecimal("30000.00")));
        assertEquals(CreditStatus.WITHIN_LIMIT, credit.getStatus());
    }

    @Test
    @DisplayName("Should throw CreditLimitExceededException when extension exceeds limit")
    void extendCredit_shouldThrowException_whenExceedsLimit() {
        Money limit = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);

        assertThrows(CreditLimitExceededException.class, () ->
                credit.extendCredit(Money.of(new BigDecimal("60000.00"), CurrencyCode.NGN))
        );
    }

    @Test
    @DisplayName("Should throw CustomerCreditBlockedException when blocked")
    void extendCredit_shouldThrowException_whenBlocked() {
        Money limit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);
        credit.block();

        assertEquals(CreditStatus.BLOCKED, credit.getStatus());
        assertThrows(CustomerCreditBlockedException.class, () ->
                credit.extendCredit(Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN))
        );
    }

    @Test
    @DisplayName("Should settle debt and update status")
    void settle_shouldReduceDebtAndSettle() {
        Money limit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);
        credit.extendCredit(Money.of(new BigDecimal("40000.00"), CurrencyCode.NGN));

        credit.settle(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN));
        assertEquals(0, credit.getOutstandingDebt().amount().compareTo(new BigDecimal("20000.00")));
        assertEquals(CreditStatus.WITHIN_LIMIT, credit.getStatus());

        credit.settle(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN));
        assertEquals(0, credit.getOutstandingDebt().amount().compareTo(BigDecimal.ZERO));
        assertEquals(CreditStatus.SETTLED, credit.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when settlement exceeds outstanding debt")
    void settle_shouldThrowException_whenAmountExceedsDebt() {
        Money limit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, limit);
        credit.extendCredit(Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN));

        assertThrows(InvalidOrderStateException.class, () ->
                credit.settle(Money.of(new BigDecimal("25000.00"), CurrencyCode.NGN))
        );
    }
}
