package com.atlashub.commerce.storefront.application.queries.GetCustomerCredit;

import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerCreditRepository;
import com.atlashub.commerce.storefront.domain.valueobject.CreditStatus;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCustomerCreditHandlerTest {

    @Mock
    private CustomerCreditRepository customerCreditRepository;

    @InjectMocks
    private GetCustomerCreditHandler handler;

    @Test
    @DisplayName("Should return customer credit details when exists")
    void execute_shouldReturnCustomerCredit() {
        Money creditLimit = Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN);
        CustomerCredit credit = CustomerCredit.create(1L, 10L, 50L, creditLimit);
        when(customerCreditRepository.findByOrganizationIdAndCustomerId(10L, 50L))
                .thenReturn(Optional.of(credit));

        CustomerCreditResult result = handler.execute(new GetCustomerCreditQuery(10L, 50L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(50L, result.customerId());
        assertEquals(creditLimit, result.creditLimit());
        assertEquals(CreditStatus.SETTLED, result.status());
    }

    @Test
    @DisplayName("Should throw CustomerCreditNotFoundException when credit does not exist")
    void execute_shouldThrowException_whenNotFound() {
        when(customerCreditRepository.findByOrganizationIdAndCustomerId(10L, 999L))
                .thenReturn(Optional.empty());

        assertThrows(CustomerCreditNotFoundException.class, () ->
                handler.execute(new GetCustomerCreditQuery(10L, 999L))
        );
    }
}
