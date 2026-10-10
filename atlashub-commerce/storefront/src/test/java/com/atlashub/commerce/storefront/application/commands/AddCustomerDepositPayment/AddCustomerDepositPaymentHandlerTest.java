package com.atlashub.commerce.storefront.application.commands.AddCustomerDepositPayment;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerDepositNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerDepositRepository;
import com.atlashub.commerce.storefront.domain.valueobject.DepositStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCustomerDepositPaymentHandlerTest {

    @Mock
    private CustomerDepositRepository depositRepository;

    @InjectMocks
    private AddCustomerDepositPaymentHandler handler;

    @Test
    @DisplayName("Should add payment to customer deposit")
    void execute_shouldAddPayment() {
        Money initialDeposit = Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN);
        Money totalAmount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        CustomerDeposit deposit = CustomerDeposit.create(1L, 100L, initialDeposit, totalAmount);

        when(depositRepository.findById(1L)).thenReturn(Optional.of(deposit));

        Money payment = Money.of(new BigDecimal("30000.00"), CurrencyCode.NGN);
        handler.execute(new AddCustomerDepositPaymentCommand(1L, payment));

        assertEquals(DepositStatus.FULFILLED, deposit.getStatus());
        assertEquals(new BigDecimal("0.0000"), deposit.getBalanceRemaining().amount());
        verify(depositRepository).save(deposit);
    }

    @Test
    @DisplayName("Should throw CustomerDepositNotFoundException when deposit not found")
    void execute_shouldThrowException_whenNotFound() {
        when(depositRepository.findById(999L)).thenReturn(Optional.empty());

        Money payment = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        assertThrows(CustomerDepositNotFoundException.class, () ->
                handler.execute(new AddCustomerDepositPaymentCommand(999L, payment))
        );
    }
}
