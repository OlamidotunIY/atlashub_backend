package com.atlashub.commerce.inventory.application.commands.CreateCustomerReturn;

import com.atlashub.commerce.inventory.domain.entities.CustomerReturn;
import com.atlashub.commerce.inventory.domain.repositories.CustomerReturnRepository;
import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateCustomerReturnHandlerTest {

    private CustomerReturnRepository customerReturnRepository;
    private CreateCustomerReturnHandler handler;

    @BeforeEach
    void setUp() {
        customerReturnRepository = mock(CustomerReturnRepository.class);
        handler = new CreateCustomerReturnHandler(customerReturnRepository);
    }

    @Test
    @DisplayName("Should create customer return successfully")
    void shouldCreateCustomerReturnSuccessfully() {
        when(customerReturnRepository.nextIdentity()).thenReturn(70L, 101L);

        CreateCustomerReturnCommand command = new CreateCustomerReturnCommand(
                1L, 2L, 3L, 4L,
                Money.of(BigDecimal.valueOf(5000), CurrencyCode.NGN),
                "Defective item",
                RefundMethod.WALLET,
                List.of(new ReturnItemDto(50L, 1))
        );

        CreateCustomerReturnResult result = handler.execute(command);
        assertNotNull(result);
        assertEquals(70L, result.returnId());
        verify(customerReturnRepository).save(any(CustomerReturn.class));
    }
}
