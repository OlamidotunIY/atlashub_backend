package com.atlashub.commerce.storefront.application.commands.ProcessCreditSale;

import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.OrderItemDto;
import com.atlashub.commerce.storefront.domain.entities.CustomerCredit;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.exceptions.CustomerCreditNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerCreditRepository;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessCreditSaleHandlerTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private CustomerCreditRepository customerCreditRepository;

    @InjectMocks
    private ProcessCreditSaleHandler handler;

    @Test
    @DisplayName("Should process credit sale, extend credit, and complete order")
    void execute_shouldProcessCreditSale() {
        CustomerCredit credit = CustomerCredit.create(
                1L, 10L, 50L,
                Money.of(new BigDecimal("100000.00"), CurrencyCode.NGN)
        );
        when(customerCreditRepository.findByOrganizationIdAndCustomerId(10L, 50L))
                .thenReturn(Optional.of(credit));

        OrderItemDto itemDto = new OrderItemDto(
                5L, null, 1,
                Money.of(new BigDecimal("20000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        ProcessCreditSaleCommand command = new ProcessCreditSaleCommand(
                10L, 20L, 50L, 30L, List.of(itemDto)
        );

        when(salesOrderRepository.nextIdentity()).thenReturn(900L);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProcessCreditSaleResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(900L, result.salesOrderId());
        assertEquals(new BigDecimal("20000.0000"), credit.getOutstandingDebt().amount());
        verify(customerCreditRepository).save(credit);
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("Should throw CustomerCreditNotFoundException when customer credit not found")
    void execute_shouldThrowException_whenNotFound() {
        when(customerCreditRepository.findByOrganizationIdAndCustomerId(10L, 50L))
                .thenReturn(Optional.empty());

        ProcessCreditSaleCommand command = new ProcessCreditSaleCommand(
                10L, 20L, 50L, 30L, List.of()
        );

        assertThrows(CustomerCreditNotFoundException.class, () -> handler.execute(command));
    }
}
