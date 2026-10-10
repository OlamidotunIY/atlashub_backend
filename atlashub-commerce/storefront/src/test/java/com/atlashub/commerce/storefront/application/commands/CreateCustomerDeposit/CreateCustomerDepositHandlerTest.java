package com.atlashub.commerce.storefront.application.commands.CreateCustomerDeposit;

import com.atlashub.commerce.storefront.domain.entities.CustomerDeposit;
import com.atlashub.commerce.storefront.domain.entities.SalesOrder;
import com.atlashub.commerce.storefront.domain.entities.SalesOrderItem;
import com.atlashub.commerce.storefront.domain.exceptions.SalesOrderNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.CustomerDepositRepository;
import com.atlashub.commerce.storefront.domain.repositories.SalesOrderRepository;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
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
class CreateCustomerDepositHandlerTest {

    @Mock
    private CustomerDepositRepository depositRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private CreateCustomerDepositHandler handler;

    @Test
    @DisplayName("Should create customer deposit and convert sales order to layaway")
    void execute_shouldCreateDeposit() {
        Money initialDeposit = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        Money totalAmount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);

        SalesOrderItem item = SalesOrderItem.create(
                1L, 100L, 10L, null, 1,
                totalAmount,
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
        SalesOrder order = SalesOrder.create(
                100L, 1L, 20L, null, null, 50L, 30L,
                OrderType.POS_RETAIL, PaymentMethod.CASH, List.of(item), null
        );

        when(salesOrderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(depositRepository.nextIdentity()).thenReturn(600L);
        when(depositRepository.save(any(CustomerDeposit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateCustomerDepositCommand command = new CreateCustomerDepositCommand(100L, initialDeposit, totalAmount);
        CreateCustomerDepositResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(600L, result.depositId());
        assertEquals(OrderStatus.LAYAWAY, order.getStatus());
        verify(salesOrderRepository).save(order);
        verify(depositRepository).save(any(CustomerDeposit.class));
    }

    @Test
    @DisplayName("Should throw SalesOrderNotFoundException when sales order not found")
    void execute_shouldThrowException_whenNotFound() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        Money amount = Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN);
        CreateCustomerDepositCommand command = new CreateCustomerDepositCommand(999L, amount, amount);

        assertThrows(SalesOrderNotFoundException.class, () -> handler.execute(command));
    }
}
